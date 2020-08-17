import PyPDF2
import nltk
import re
from io import StringIO
from pdfminer.converter import TextConverter
from pdfminer.layout import LAParams
from pdfminer.pdfdocument import PDFDocument
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.pdfpage import PDFPage
from pdfminer.pdfparser import PDFParser
import io

def convert_pdf_to_string(file_path):

	output_string = StringIO()
	with open(file_path, 'rb') as in_file:
	    parser = PDFParser(in_file)
	    doc = PDFDocument(parser)
	    rsrcmgr = PDFResourceManager()
	    device = TextConverter(rsrcmgr, output_string, laparams=LAParams())
	    interpreter = PDFPageInterpreter(rsrcmgr, device)
	    for page in PDFPage.create_pages(doc):
	        interpreter.process_page(page)

	return(output_string.getvalue())






text= convert_pdf_to_string('power-up-your-mind-learn-fasterwork-smarter-bill-lucas.pdf')
tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')
txt= (tokenizer.tokenize(text))

with io.open('out1.txt', "w", encoding="utf-8") as f:
  for i in txt:
    i = i.replace('\n','')	  
    f.write(i)
    f.write('\n')


f.close()

