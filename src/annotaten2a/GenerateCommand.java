package annotaten2a;

import java.io.File;

public class GenerateCommand {

	public static String path = "C:\\Users\\kunal\\workspace\\n2a-batch\\lib";
	public static void main(String[] args) {
		File directory = new File(path);
		String[] children = directory.list();
		String command = "";
		for (String fileName : children) {
			if(command.length() == 0){
				command = path + "\\" + fileName;
			}
			else{
				command = command + ";" + path + "\\" + fileName;
			}
		}
		System.out.println(command);

	}
}
