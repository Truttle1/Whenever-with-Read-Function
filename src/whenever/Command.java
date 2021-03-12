package whenever;
import java.io.*;
import java.util.*;

class Command
{
	private static final String PRINT = "print";
	private String deferString = null; // the defer clause, without enclosing parentheses
	private String againString = null; // the again clause, without enclosing parentheses
	private String actionString = null; // the command to be executed, without terminating semi-colon
	public int numToDo = 1;

	public void setDeferString(String s)
	{
		deferString = s;
	}

	public void setAgainString(String s)
	{
		againString = s;
	}

	public void setActionString(String s)
	{
		actionString = s;
	}
	
	public void execute(Integer lineNumber)
	{
		try
		{
			if (this.evaluateBoolean(deferString))
				return;
			this.action();
			if (!this.evaluateBoolean(againString))
				WheneverCode.code.changeNumToDo(lineNumber, -1);
		}
		catch (SyntaxException e)
		{
			System.out.println("Syntax error detected at runtime in line numbered " + lineNumber.toString());
			System.out.println(e.getMessage() + "\n");
			System.exit(0);
		}
	}

	/*
	 * Evaluates the argument string as a boolean expression.
	 */
	private boolean evaluateBoolean(String s) throws SyntaxException
	// ******** HAVE TO IMPLEMENT UNARY NOT **********
	{
		if (s == null || s.equals("false"))
			return false;
		if (s.equals("true"))
			return true;
		if (WheneverCode.traceLevel > 3)
			System.out.println("[Boolean eval: " + s + " ]");
		// check for numerical comparisons
		int less = s.indexOf("<");
		int greater = s.indexOf(">");
		int equals = s.indexOf("==");
		int notequals = s.indexOf("!=");
		int i = -1;
		if (less >= 0 && (i < 0 || less < i))
			i = less;
		if (greater >= 0 && (i < 0 || greater < i))
			i = greater;
		if (equals >= 0 && (i < 0 || equals < i))
			i = equals;
		if (notequals >= 0 && (i < 0 || notequals < i))
			i = notequals;
		if (i >= 0) // we have a numerical comparison - evaluate it and substitute it into the string
		{
			int lside = Math.max(s.substring(0,i).lastIndexOf("&&"), s.substring(0,i).lastIndexOf("||"));
			int rside1 = s.indexOf("&&", i);
			int rside2 = s.indexOf("||", i);
			int rside = -1;
			if (rside1 >= 0 && rside2 >= 0)
				rside = Math.min(rside1, rside2);
			if (rside1 >= 0 && rside2 < 0)
				rside = rside1;
			if (rside1 < 0 && rside2 >= 0)
				rside = rside2;
			String lstring = "";
			String rstring = "";
			String lcomp;
			String rcomp;
			String op = s.substring(i,i+2);
			// extract left comparison operand and left boolean leftover string if necessary
			if (lside >= 0)
			{
				lside += 2;
				lstring = s.substring(0,lside);
				lcomp = s.substring(lside,i).trim();
			}
			else
				lcomp = s.substring(0,i).trim();
			// add different amount to index to extract right operand for different length operators
			if (op.equals("<=") || op.equals(">=") || op.equals("==") || op.equals("!="))
				i += 2;
			else // simple < or >
				i++;
			// extract right comparison operand and right boolean leftover string if necessary
			if (rside >= 0)
			{
				rstring = s.substring(rside);
				rcomp = s.substring(i,rside).trim();
			}
			else
				rcomp = s.substring(i).trim();
			boolean comp;
			if (op.equals("<="))
				comp = evaluateInteger(lcomp) <= evaluateInteger(rcomp);
			else if (op.equals(">="))
				comp = evaluateInteger(lcomp) >= evaluateInteger(rcomp);
			else if (op.equals("=="))
				comp = evaluateInteger(lcomp) == evaluateInteger(rcomp);
			else if (op.equals("!="))
				comp = evaluateInteger(lcomp) != evaluateInteger(rcomp);
			else if (op.charAt(0) == '<')
				comp = evaluateInteger(lcomp) < evaluateInteger(rcomp);
			else // >
				comp = evaluateInteger(lcomp) > evaluateInteger(rcomp);
			return evaluateBoolean(lstring + String.valueOf(comp) + rstring);
		}
		// check for or operator
		i = s.indexOf("||");
		if (i >= 0)
			return evaluateBoolean(s.substring(0,i).trim()) || evaluateBoolean(s.substring(i+2).trim());
		// we must have an and operator
		i = s.indexOf("&&");
		if (i >= 0)
			return evaluateBoolean(s.substring(0,i).trim()) && evaluateBoolean(s.substring(i+2).trim());
		// exhausted all boolean operations, must have an integer expression which is evaluated according to to-do list
		return WheneverCode.code.getN(evaluateInteger(s));
	}

	/*
	 * Performs the action expression in the actionString.
	 */
	private void action() throws SyntaxException
	{
		if (actionString.indexOf(PRINT) == 0)
			doPrint(actionString.substring(PRINT.length()).trim());
		else
			doLines(actionString);
		return;
	}

	/*
	 * Adds or subtracts lots of lines from the to-do list according to the specifications in the argument.
	 */
	private void doLines(String s) throws SyntaxException
	{
		StringTokenizer tok = new StringTokenizer(s, ",");
		while (tok.hasMoreTokens())
			doLine(tok.nextToken());
	}

	/*
	 * Adds or subtracts a line from the to-do list according to the specifications in the argument.
	 */
	private void doLine(String s) throws SyntaxException
	{
		int numTimes = 1;
		int lineNumber = 0;
		int i;
		if ((i = s.indexOf("#")) >= 0)
		{
			numTimes = evaluateInteger(s.substring(i+1));
			lineNumber = evaluateInteger(s.substring(0,i));
		}
		else
			lineNumber = evaluateInteger(s);
		if (WheneverCode.traceLevel >= 2)
			System.out.println("[Adding line " + String.valueOf(lineNumber) + ", " + String.valueOf(numTimes) + " times]");
		WheneverCode.code.changeNumToDo(lineNumber, numTimes);
	}

	/*
	 * Evaluates the argument string as an integer expression.
	 */
	private int evaluateInteger(String s) throws SyntaxException
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			//System.out.println(">" + s);
			if (WheneverCode.traceLevel > 4)
			{
				System.out.println("[passed in: " + s + "]");
			}
			int i, j;

			/*if((i = s.indexOf("read()")) >= 0)
			{
				j = i + 1;
				int depth = 1;
				Scanner sc = new Scanner(System.in);
				String str = sc.next();
				int result = -1;
				try
				{
					result = Integer.parseInt(str);
				}
				catch(Exception ex)
				{
					result = (int)str.charAt(0);
				}
				return evaluateInteger(s.substring(0,i) + "(" + result + ")" + s.substring(i+6));
				//return Integer.parseInt(sc.next());
			}
			else */if ((i = s.indexOf("N(")) >= 0)
			{
				j = i + 1;
				int depth = 1;
				try
				{
					while (depth > 0)
					{
						j++;
						if (s.charAt(j) == '(')
							depth++;
						if (s.charAt(j) == ')')
							depth--;
					}
				}
				catch (StringIndexOutOfBoundsException ex)
				{
					throw new SyntaxException("Mismatched parentheses in arithmetic expression function call");
				}
				if (WheneverCode.traceLevel > 3)
				{
					System.out.println("[N() argument: " + s.substring(i+2,j) + "]");
					System.out.println("[N() substituted: " + s.substring(0,i) + String.valueOf(WheneverCode.code.getNumToDo(evaluateInteger(s.substring(i+2,j)))) + s.substring(j+1) + "]");
				}
				return evaluateInteger(s.substring(0,i) + String.valueOf(WheneverCode.code.getNumToDo(evaluateInteger(s.substring(i+2,j)))) + s.substring(j+1));
			}
			else if ((i = s.indexOf("(")) >= 0)
			{
				j = i;
				int depth = 1;
				try
				{
					while (depth > 0)
					{
						j++;
						if (s.charAt(j) == '(')
							depth++;
						if (s.charAt(j) == ')')
							depth--;
					}
				}
				catch (StringIndexOutOfBoundsException ex)
				{
					throw new SyntaxException("Mismatched parentheses in arithmetic expression");
				}
				if (WheneverCode.traceLevel > 3)
				{
					System.out.println("[() argument: " + s.substring(i+1,j) + "]");
					System.out.println("[() substituted: " + s.substring(0,i) + String.valueOf(evaluateInteger(s.substring(i+1,j))) + s.substring(j+1) + "]");
				}
				return evaluateInteger(s.substring(0,i) + String.valueOf(evaluateInteger(s.substring(i+1,j))) + s.substring(j+1));
			}
			else if (s.indexOf('+') >= 0 || s.indexOf('-') >= 0) // we have + and/or - signs
			{
				try
				{
					StreamTokenizer tok = new StreamTokenizer(new StringReader(s));
					if (tok.nextToken() != StreamTokenizer.TT_NUMBER)
						throw new SyntaxException("Bad arithmetic expression (1)");
					String start = String.valueOf((int)tok.nval);
					tok.nextToken();
					while ((char)tok.ttype != '+' && (char)tok.ttype != '-')
					{
						if (tok.ttype == StreamTokenizer.TT_NUMBER)
							start += String.valueOf((int)tok.nval);
						else
							start += String.valueOf((char)tok.ttype);
						tok.nextToken();
						if (tok.ttype == StreamTokenizer.TT_EOF) // only unary minuses!
							return multiply(s);
					}
					int op = tok.ttype;
					String remainder = "";
					while (tok.nextToken() != StreamTokenizer.TT_EOF)
					{
						if (tok.ttype == StreamTokenizer.TT_NUMBER)
							remainder += String.valueOf((int)tok.nval);
						else if (tok.ttype > 0)
							remainder += String.valueOf((char)tok.ttype);
						else
							throw new SyntaxException("Bad arithmetic expression (3)");
					}
					if (WheneverCode.traceLevel > 3)
						System.out.println("[arguments: (" + start + ") " + String.valueOf((char)op) + " (" + remainder + ") ]");
					if ((char)op == '+')
						return evaluateInteger(start) + evaluateInteger(remainder);
					else
						return evaluateInteger(start) - evaluateInteger(remainder);
				}
				catch (IOException exc)
				{
					throw new SyntaxException("I/O exception reading arithmetic expression");
				}
			}
			else // we have * and/or / signs
				return multiply(s);
		}
	}

	/*
	 * Evaluates the argument string as an integer expression.
	 * Argument string may only contain * and / binary operators and unary minuses.
	 */
	private int multiply(String s) throws SyntaxException
	{
		int i = s.indexOf("*");
		int j = s.indexOf("/");
		if (i >= 0 && (j < 0 || i < j))
		{
			if (WheneverCode.traceLevel > 3)
				System.out.println("[* arguments: " + s.substring(0,i).trim() + ", " + s.substring(i+1).trim() + "]");
			return evaluateInteger(s.substring(0,i).trim()) * evaluateInteger(s.substring(i+1).trim());
		}
		else
		{
			if (WheneverCode.traceLevel > 3)
				System.out.println("[/ arguments: " + s.substring(0,j).trim() + ", " + s.substring(j+1).trim() + "]");
			return (int)(evaluateInteger(s.substring(0,j).trim()) / evaluateInteger(s.substring(j+1).trim()));
		}
	}

	/*
	 * Prints the String expression specified by the argument.
	 */
	private void doPrint(String s) throws SyntaxException
	{
		if (s.charAt(0) != '(' || s.charAt(s.length()-1) != ')')
			throw new SyntaxException("Print statement must be enclosed in parentheses");
		s = s.substring(1, s.length()-1).trim();
		//System.out.println(s);
		// construct the output string by concatenating items
		String out = "";
		while (s.length() > 0)
		{
			// if item begins with a string literal it begins with a double quote
			if (s.charAt(0) == '"')
			{
				// get index of closing double quote
				int i = s.indexOf('"', 1);
				if (i < 0) // make sure quote is closed
					throw new SyntaxException("String must be delimited by double quotes");
				// add string literal to output
				out += s.substring(1, i);
				// check if any more stuff needs to be added
				if (i+1 == s.length())
					s = ""; // entire output string is processed
				else
				{ // there is more to be concatenated
					s = s.substring(i+1).trim();
					// check for concatenation operator
					if (s.charAt(0) != '+')
						throw new SyntaxException("Illegal string concatenation, + expected");
					// remove + sign
					s = s.substring(1).trim();
				}
			}
			else // item does not begin with a string literal
			{
				// get index of opening double quote
				int i = s.indexOf('"');
				if (i < 0)
				{ // there are no more string literals - all stuff is an integer expression
					out += String.valueOf(evaluateInteger(s));
					s = "";
				}
				else
				{ // there is a concatenated string literal
					// check for concatenation operator
					if (s.charAt(s.substring(0,i).trim().length()-1) != '+')
						throw new SyntaxException("Illegal string concatenation, + expected");
					out += evaluateInteger(s.substring(0,s.substring(0,i).trim().length()-1).trim());
					s = s.substring(i);
				}
			}
		}
		// print the output!
		System.out.println(out);
	}
}
