import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatFilter {
    /*
     *This class will handle the "bad words" filter works.
     * 1. make a new arrayList for words from the txt file.
     * 2. ChartFilter function:
     * 		- open the txt file ( list of the bad words )
     * 		- read line by line and put the words into the arrayList
     * 3. filter function:
     * 		- change the bad words to * sign
     *
     */ 
    private ArrayList<String> badwords  = new ArrayList<>();
    public ChatFilter(String badWordsFileName) {
        try
        {
            File f = new File(badWordsFileName);
            Scanner s = new Scanner(f);
            while(s.hasNext())
            {
                badwords.add(s.nextLine());
            }

        }catch (IOException e)
        {
            System.out.println("No file found.");
        }
    }
    //return the size of the ArrayList
    public int getBadWorsSize()
    {
        return badwords.size();
    }

    public void printBadWords()
    {
        for(int i = 0; i < badwords.size(); i++)
        {
            System.out.println(badwords.get(i));
        }
    }

    public String filter(String msg) {
        for(int i = 0; i < badwords.size(); i++)
        {
            String filtering = badwords.get(i);
            String filtered = "";
            for(int j = 0; j < filtering.length(); j++)
            {
                filtered += "*";
            }
            for(int j = 0; j + filtering.length() <= msg.length(); j++)
            {
                if(msg.substring(j,j + filtering.length()).toLowerCase().equals(filtering.toLowerCase()))
                {
                    msg = msg.replace(msg.substring(j,j + filtering.length()), filtered);
                }
            }
        }
        return msg;
    }
}
