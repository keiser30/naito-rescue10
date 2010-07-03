import java.io.*;

public class makeClassPath
{
	public static void main(String[] args){
		
		if(args.length <= 0){
			System.err.println("Usage: java makeClassPath <jar_dir>");
			System.exit(-1);
		}
		StringBuffer classpath = new StringBuffer();
		File jarDir = new File(args[0]);
		File[] jarFiles = jarDir.listFiles();
		
		for(int i = 0;i < jarFiles.length-1;i++){
			System.err.println(jarFiles[i].getName());
			if(jarFiles[i].getPath().endsWith(".jar"))
				classpath.append(jarFiles[i].getPath()).append(";");
		}
		classpath.append(jarFiles[jarFiles.length-1].getPath());
		System.out.println("--------------------");
		System.out.println(classpath.toString());
	}
}
