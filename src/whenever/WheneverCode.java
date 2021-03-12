package whenever;
import java.io.*;

class WheneverCode {
	private static final String DEFER = "defer";
	private static final String AGAIN = "again";
	static CodeTable code;
	static int traceLevel = 0;

	public WheneverCode(String infile)
	{
		this(infile, "0");
	}

	public WheneverCode(String infile, String t)
	{
		try
		{
			this.traceLevel = Integer.parseInt(t);
		}
		catch (NumberFormatException e)
		{
			System.out.println("Illegal trace level specified - use an integer.");
			System.exit(0);
		}
		code = new CodeTable();
		try
		{
			FileReader fileIn = new FileReader(infile);
			LineNumberReader in = new LineNumberReader(fileIn);
			String line;
			try
			{
				while ((line = in.readLine()) != null)
					parse(line.trim());
				System.out.println(String.valueOf(in.getLineNumber()) + " lines successfully read from source file " + infile);
				fileIn.close();
			}
			catch (SyntaxException e)
			{
				System.out.println("Syntax error in program source file " + infile + " at line " + String.valueOf(in.getLineNumber()));
				System.out.println(e.getMessage() + "\n");
				System.exit(0);
			}
			catch (Exception e)
			{
				System.out.println("Exception reading program source file.\n");
				System.out.println(e.toString() + "\n");
				System.exit(0);
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Program source file " + infile + " not found.\n");
			System.exit(0);
		}
	}

	private void parse(String line) throws SyntaxException
	{
		int space = line.indexOf(" ");
		int tab = line.indexOf("\t");
		if (space < 0 && tab < 0)
			throw new SyntaxException("Missing line number");
		Integer lineNumber = null;
		try
		{
			lineNumber = new Integer(line.substring(0, (tab < 0 || tab > space)?space:tab));
		}
		catch (NumberFormatException e)
		{
			throw new SyntaxException("Bad line number format");
		}
		line = line.substring((tab < 0 || tab > space)?space:tab).trim(); // trim off line number and whitespace
		Command command = new Command();
		int endDefer = 0, endAgain = 0;
		int i = line.indexOf(DEFER);
		if (i >= 0)
		{ // there is a defer clause
			endDefer = i = line.indexOf('(', i);
			int nest = 1;
			try
			{
				while (nest > 0)
				{
					if (line.charAt(++endDefer) == ')')
						nest--;
					if (line.charAt(endDefer) == '(')
						nest++;
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				throw new SyntaxException("Defer clause requires matched parentheses");
			}
			command.setDeferString(line.substring(i+1, endDefer++));
		}
		i = line.indexOf(AGAIN);
		if (i >= 0)
		{ // there is an again clause
			endAgain = i = line.indexOf('(', i);
			int nest = 1;
			try
			{
				while (nest > 0)
				{
					if (line.charAt(++endAgain) == ')')
						nest--;
					if (line.charAt(endAgain) == '(')
						nest++;
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				throw new SyntaxException("Again clause requires matched parentheses");
			}
			command.setAgainString(line.substring(i+1, endAgain++));
		}
		line = line.substring(Math.max(endDefer, endAgain)).trim();
		if (line.charAt(line.length()-1) != ';')
			throw new SyntaxException("Line requires terminating semi-colon");
		command.setActionString(line.substring(0, line.length()-1));
		// put the line into the code table
		code.put(lineNumber, command);
	}

	public void run()
	{
		while (code.getTotalToDo() > 0)
		{
			int totalToDo = code.getTotalToDo();
			int instanceToDo = (int)(Math.random() * totalToDo) + 1;
			code.doCommand(instanceToDo);
		}
	}
}
