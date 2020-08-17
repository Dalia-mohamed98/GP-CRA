import xlwt 
from xlwt import Workbook 
  
# Workbook is created 
wb = Workbook() 
row = 0
# add_sheet is used to create sheet. 
sheet1 = wb.add_sheet('Sheet 1',cell_overwrite_ok=True)
fp = open("test.txt",encoding="utf8")
lines = fp.readlines()
j = 0
for line in lines:
    col = 0
    myString = line.rstrip()
    myString = myString.split(',')
    if len(myString ) < 2:
        
        myStr = "LJ687-" + str(j) + "|" + myString[0] + "|" + myString[0]
        sheet1.write(row,col,myStr)        
        j += 1
        row += 1
        continue

    for subStr in myString:
        if col == 0:
            subStr = "LJ687-" + str(j) + "|" + subStr
            
        sheet1.write(row,col,subStr)
        col += 1
        
    col -= 1
    sheet1.write(row,col,myString[len(myString)-1]+"|"+myString[0])
    col += 1
    i = 1
    while i < len(myString):
        sheet1.write(row,col,myString[i])
        col += 1
        i += 1
    row += 1
    j += 1
wb.save('xlwt example.xls')
