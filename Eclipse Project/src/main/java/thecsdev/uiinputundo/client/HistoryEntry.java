package thecsdev.uiinputundo.client;

/**
 * Not implemented yet. Planning to implement it.
 */
public class HistoryEntry
{
	public final String text;
	public final int cursorPosition;
	
	public HistoryEntry(String text, int cursorPosition)
	{
		if(text == null) text = "";
		this.text = text;
		this.cursorPosition = cursorPosition;
	}
}