package thecsdev.uiinputundo.mixin;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.obfuscate.DontObfuscate;
import thecsdev.uiinputundo.client.HistoryEntry;
import thecsdev.uiinputundo.client.UIInputUndoClient;

@Mixin(EditBox.class)
public abstract class EditBoxMixin
{
	// ==================================================
	public final ArrayList<HistoryEntry> UndoHistory = new ArrayList<>();
	public final ArrayList<HistoryEntry> RedoHistory = new ArrayList<>();
	public HistoryEntry LastUndoEntry = null;
	private boolean Undoing = false;
	// ==================================================
	@Inject(at = @At("TAIL"), method = "onValueChange")
	public void onValueChange(String newText, CallbackInfo callback)
	{
		//avoid null newText and registering undo when undoing/redoing
		if(newText == null || Undoing) return;
		//avoid registering undo same texts
		else if(LastUndoEntry != null && StringUtils.equals(LastUndoEntry.text, newText)) return;
				
		//handle last entry
		if(LastUndoEntry == null)
		{
			LastUndoEntry = HistoryEntry.empty();
			if(UndoHistory.size() == 0) UndoHistory.add(LastUndoEntry.clone());
		}
		
		//register undo and clear redo
		registerUndo(LastUndoEntry);
		LastUndoEntry = new HistoryEntry(newText, getCursorPosition());
		RedoHistory.clear();
	}
	// ==================================================
	@Invoker("getValue") public abstract String getValue();
	@Invoker("setValue") public abstract void setValue(String value);
	@Invoker("getCursorPosition") public abstract int getCursorPosition();
	@Invoker("setCursorPosition") public abstract void setCursorPosition(int cursor);
	private final boolean isActiveB() { return ((EditBox)(Object)this).isActive(); }
	// --------------------------------------------------
	@Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback)
	{
		//check if active
		if(!isActiveB()) return;
		
		//check if control is down
		if(!Screen.hasControlDown()) return;
		
		//check for undo
		if(UIInputUndoClient.KeyUndo.matches(keyCode, scanCode))
		{
			if(!Screen.hasShiftDown()) undo();
			else undo(true);
			callback.setReturnValue(true); callback.cancel(); return;
		}
		
		//check for redo
		else if(UIInputUndoClient.KeyRedo.matches(keyCode, scanCode))
		{
			if(!Screen.hasShiftDown()) redo();
			else redo(true);
			callback.setReturnValue(true); callback.cancel(); return;
		}
	}
	// ==================================================
	public void registerUndo(HistoryEntry entry)
	{
		//check last entry
		if(UndoHistory.size() > 0 && UndoHistory.get(UndoHistory.size() - 1).text.equals(entry.text))
			return;
		
		//add undo
		UndoHistory.add(entry.clone());
		
		//limit undo size
		if(UndoHistory.size() > UIInputUndoClient.HistorySize)
			UndoHistory.remove(0);
	}
	
	public void registerRedo(HistoryEntry entry)
	{
		//check first entry
		if(RedoHistory.size() > 0 && RedoHistory.get(0).text.equals(entry.text))
			return;
		
		//add redo
		RedoHistory.add(0, entry.clone());
		
		//limit undo size
		if(RedoHistory.size() > UIInputUndoClient.HistorySize)
			RedoHistory.remove(RedoHistory.size() - 1);
	}
	// --------------------------------------------------
	@DontObfuscate
	public void undo() { undo(false); }
	@DontObfuscate
	public void undo(boolean undoSingle)
	{
		//check undo history size
		if(UndoHistory.size() < 1)
			return;
		
		Undoing = true;
		
		HistoryEntry oldText = null, text = null;
		do
		{
			oldText = new HistoryEntry(getValue(), getCursorPosition());
			//obtain last entry
			text = UndoHistory.get(UndoHistory.size() - 1);
			UndoHistory.remove(UndoHistory.size() - 1);
			if(text == null) break;
			
			registerRedo(LastUndoEntry != null ? LastUndoEntry : HistoryEntry.empty());
			LastUndoEntry = text;
			
			//set text
			setValue(text.text);
			setCursorPosition(text.cursorPosition);
			if(!oldText.text.startsWith(text.text)) break;
		}
		while(!undoSingle && (UndoHistory.size() > 0 && uiinputundo_keepUndoing(text)));
		
		Undoing = false;
	}
	
	@DontObfuscate
	public void redo() { redo(false); }
	@DontObfuscate
	public void redo(boolean redoSingle)
	{
		//check redo history size
		if(RedoHistory.size() < 1)
			return;
		
		Undoing = true;
		
		HistoryEntry oldText = null, text = null;
		do
		{
			oldText = new HistoryEntry(getValue(), getCursorPosition());
			//obtain first entry
			text = RedoHistory.get(0);
			RedoHistory.remove(0);
			if(text == null) break;
			
			registerUndo(LastUndoEntry != null ? LastUndoEntry : HistoryEntry.empty());
			LastUndoEntry = text;
			
			//set text
			setValue(text.text);
			setCursorPosition(text.cursorPosition);
			if(!text.text.startsWith(oldText.text)) break;
		}
		while(!redoSingle && (RedoHistory.size() > 0 && uiinputundo_keepUndoing(text)));
		
		Undoing = false;
	}
	// --------------------------------------------------
	private boolean uiinputundo_keepUndoing(HistoryEntry arg0)
	{
		try { return Character.isLetter(arg0.text.charAt(arg0.cursorPosition - 1)); }
		catch(Exception e) { return false; }
	}
	// --------------------------------------------------
	/*private void uiinputundo_replaceSelection(Function<String, String> func)
	{
		//get selection indexes and selection text
		int i = Math.min(getSelectionStart(), getSelectionEnd());
	    int j = Math.max(getSelectionStart(), getSelectionEnd());
	    String selectedText = null;
	    
	    try { selectedText = getValue().substring(i, j); }
	    catch(Exception e) {}
	    
	    //begin
	    if(!StringUtils.isEmpty(selectedText))
	    {
	    	//if there is text selected, only apply to selected text
	    	int i1 = getSelectionStart();
	    	int j1 = getSelectionEnd();
	    	int cursor = getCursorPosition();
	    	
	    	selectedText = func.apply(selectedText);
	    	String output = new StringBuilder(getValue()).replace(i, j, selectedText).toString();
	    	
	    	if(!getTextPredicate().test(output)) return;
	    	setValue(output);
	    	
	    	setCursorPosition(cursor);
	    	setSelectionStart(i1);
	    	setSelectionEnd(j1);
	    }
	    else
	    {
	    	//if there is no selected text, apply to the whole text
	    	int cursor = getCursorPosWithOffset(0);
	    	
	    	String output = func.apply(getValue());
	    	if(!getTextPredicate().test(output)) return;
	    	setValue(output);
	    	
	    	setCursorPosition(cursor);
	    }
	}*/
	// ==================================================
}
