package annotaten2a;

import java.io.Console;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexTestHarness {

    public static void main(String[] args){
        while (true) {

            Pattern pattern = 
            Pattern.compile("^[\\s]*public[\\s]*[\\w]*[\\s]*get[\\w]*");

            Matcher matcher = 
            	//pattern.matcher("  public   Integer  " );
            pattern.matcher("public String getDescription() {");

            boolean found = false;
            while (matcher.find()) {
                System.out.println("I found the text \"%s\" starting at " +
                   "index %d and ending at index %d.%n");
                found = true;
            }
            if(!found){
            	System.out.println("No match found.%n");
            }
        }
    }
}
