package whenever;
public class Whenever
{
	private static WheneverCode code;

	public static void main(String[] args)
	{
		if (args.length == 2)
		{
			code = new WheneverCode(args[0], args[1]);
			code.run();
		}
		else if (args.length == 1)
		{
			code = new WheneverCode(args[0]);
			code.run();
		}
		else
		{
			System.out.println("Usage: java Whenever <sourcefile> [tracelevel]\n");
			System.exit(0);
		}
	}
}
