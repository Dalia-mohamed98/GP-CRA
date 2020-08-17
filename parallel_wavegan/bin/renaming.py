import os 
  
# Function to rename multiple files 
def main(): 
    i = 89
    for count, filename in enumerate(os.listdir("temp")): 
        tempName = ""
        src=""
        dst=""
        if(len(filename) < 10):
            tempName =  filename[:4]+"00"+filename[4:]
            dst = tempName 
            src ='temp/'+ filename 
            dst ='temp/'+ dst 
            os.rename(src, dst) 
        elif(len(filename) < 11):
            tempName =  filename[:4]+"0"+filename[4:]
            dst = tempName 
            src ='temp/'+ filename 
            dst ='temp/'+ dst   
            os.rename(src, dst) 
        #dst ="658-" + str(i) + ".wav"
        
        
        # rename() function will 
        # rename all the files 
        i+=1
  
# Driver Code 
if __name__ == '__main__': 
      
    # Calling main() function 
    main()