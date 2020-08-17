
import math
import torch
import torch.nn.functional as F


class Conv1d(torch.nn.Conv1d):
   

    def __init__(self, *args, **kwargs):
        super(Conv1d, self).__init__(*args, **kwargs)

    def reset_parameters(self):
        torch.nn.init.kaiming_normal_(self.weight, nonlinearity="relu")
        if self.bias is not None:
            torch.nn.init.constant_(self.bias, 0.0)


class Conv1d1x1(Conv1d):
    def __init__(self, in_channels, out_channels, bias):
        super(Conv1d1x1, self).__init__(in_channels, out_channels,kernel_size=1, padding=0,dilation=1, bias=bias)


class ResidualBlock(torch.nn.Module):

    def __init__(self, kernel_size=3,residual_channels=64, gate_channels=128, skip_channels=64, aux_channels=80, dropout=0.0, dilation=1,
                 bias=True,use_causal_conv=False ):
       
        super(ResidualBlock, self).__init__()
        self.dropout = dropout
        # no future time stamps available
        if use_causal_conv:
            padding = (kernel_size - 1) * dilation
        else:
            assert (kernel_size - 1) % 2 == 0, 
            padding = (kernel_size - 1) // 2 * dilation
        self.use_causal_conv = use_causal_conv

        # dilation conv
        self.conv = Conv1d(residual_channels, gate_channels, kernel_size,padding=padding, dilation=dilation, bias=bias)

        # local conditioning
        if aux_channels > 0:
            self.conv1x1_aux = Conv1d1x1(aux_channels, gate_channels, bias=False)
        else:
            self.conv1x1_aux = None

        # conv output is split into two groups
        gate_out_channels = gate_channels // 2
        self.conv1x1_out = Conv1d1x1(gate_out_channels, residual_channels, bias=bias)
        self.conv1x1_skip = Conv1d1x1(gate_out_channels, skip_channels, bias=bias)

    def forward(self, x, c):
        residual = x
        x = F.dropout(x, p=self.dropout, training=self.training)
        x = self.conv(x)

        # remove future time steps if use_causal_conv conv
        x = x[:, :, :residual.size(-1)] if self.use_causal_conv else x

        # split into two part for gated activation
        splitdim = 1
        xa, xb = x.split(x.size(splitdim) // 2, dim=splitdim)

        # local conditioning
        if c is not None:
            assert self.conv1x1_aux is not None
            c = self.conv1x1_aux(c)
            ca, cb = c.split(c.size(splitdim) // 2, dim=splitdim)
            xa, xb = xa + ca, xb + cb

        x = torch.tanh(xa) * torch.sigmoid(xb)

        # for skip connection
        s = self.conv1x1_skip(x)

        # for residual connection
        x = (self.conv1x1_out(x) + residual) * math.sqrt(0.5)

        return x, s