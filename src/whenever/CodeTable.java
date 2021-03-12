package whenever;

import java.util.*;

class CodeTable extends Hashtable
{
	private int totalToDo = 0;

	public int getTotalToDo()
	{
		return totalToDo;
	}

	/*
	 * Override put() method of Hashtable
	 */
	public synchronized Object put(Object lineNumber, Object value)
	{
		Command command = (Command)value;
		Object old = super.put(lineNumber, command);
		if (old == null)
		{
			totalToDo += command.numToDo;
			return null;
		}
		else
		{
			Command oldCommand = (Command)old;
			totalToDo += command.numToDo - oldCommand.numToDo;
			return oldCommand;
		}
	}

	public boolean getN(int lineNum)
	{
		Integer lineNumber = new Integer(lineNum);
		return getN(lineNumber);
	}

	public boolean getN(Integer lineNumber)
	{
		Command command = (Command)this.get(lineNumber);
		if (command == null)
			return false;
		return command.numToDo > 0;
	}

	public int getNumToDo(int lineNum)
	{
		Integer lineNumber = new Integer(lineNum);
		return getNumToDo(lineNumber);
	}

	public int getNumToDo(Integer lineNumber)
	{
		Command command = (Command)this.get(lineNumber);
		if (command == null)
			return 0;
		return command.numToDo;
	}

	public int changeNumToDo(int lineNum, int change)
	{
		Integer lineNumber = new Integer(lineNum);
		return changeNumToDo(lineNumber, change);
	}

	public int changeNumToDo(Integer lineNumber, int change)
	{
		if (lineNumber.intValue() < 0)
		{
			lineNumber = new Integer(-lineNumber.intValue());
			change = -change;
		}
		Command command = (Command)this.get(lineNumber);
		if (command == null) // need to do something here to implement adding extra line numbers
			return 0;
		command.numToDo += change;
		totalToDo += change;
		if (command.numToDo < 0)
		{
			totalToDo -= command.numToDo;
			command.numToDo = 0;
		}
		this.put(lineNumber, command);
		return command.numToDo;
	}

	public void doCommand(int instanceToDo) throws NoSuchElementException
	{
		// get the actual command to be executed by iterating through the commands until we pass the required number
		Enumeration e = this.keys();
		Integer lineNumber = (Integer)e.nextElement();
		Command command = (Command)this.get(lineNumber);
		while (instanceToDo > command.numToDo)
		{
			instanceToDo -= command.numToDo;
			lineNumber = (Integer)e.nextElement();
			command = (Command)this.get(lineNumber);
		}
		if (WheneverCode.traceLevel >= 1)
			System.out.println("[Attempting line " + lineNumber.toString() + "]");
		// execute the command, passing the line number so it know what its line number is
		command.execute(lineNumber);
	}
}