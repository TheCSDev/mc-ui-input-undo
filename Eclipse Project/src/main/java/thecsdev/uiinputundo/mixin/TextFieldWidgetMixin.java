package thecsdev.uiinputundo.mixin;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.obfuscate.DontObfuscate;
import thecsdev.uiinputundo.client.HistoryEntry;
import thecsdev.uiinputundo.client.UIInputUndoClient;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin
{
	// ==================================================
	public final ArrayList<HistoryEntry> UndoHistory = new ArrayList<>();
	public final ArrayList<HistoryEntry> RedoHistory = new ArrayList<>();
	public HistoryEntry LastEntry = null;
	private boolean Undoing = false;
	// ==================================================
	@Inject(at = @At("TAIL"), method = "onChanged")
	public void onChanged(String newText, CallbackInfo callback)
	{
		//avoid null newText and registering undo when undoing/redoing
		if(newText == null || Undoing) return;
		//avoid registering undo same texts
		else if(LastEntry != null && StringUtils.equals(LastEntry.text, newText)) return;
				
		//handle last entry
		if(LastEntry == null)
		{
			LastEntry = HistoryEntry.empty();
			if(UndoHistory.size() == 0) UndoHistory.add(LastEntry.clone());
		}
		
		//register undo and clear redo
		registerUndo(LastEntry);
		LastEntry = new HistoryEntry(newText, getCursorPosWithOffset(0));
		RedoHistory.clear();
	}
	// --------------------------------------------------
	@Accessor("text") public abstract String getText();
	@Invoker("setText") public abstract void setText(String text);
	
	@Invoker("isActive")       public abstract boolean isActive();
	@Invoker("setCursorToEnd") public abstract void setCursorToEnd();
	@Invoker("getCursorPosWithOffset") public abstract int getCursorPosWithOffset(int offset);
	@Invoker("setCursor") public abstract void setCursor(int cursor);
	// --------------------------------------------------
	@Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback)
	{
		//check if active
		if(!isActive()) return;
		
		//check if control is down
		if(!Screen.hasControlDown()) return;
		
		//check for undo
		if(UIInputUndoClient.KeyUndo.matchesKey(keyCode, scanCode))
		{
			if(!Screen.hasShiftDown()) undo();
			else undo(true);
			
			callback.setReturnValue(true);
			callback.cancel();
			return;
		}
		
		//check for redo
		else if(UIInputUndoClient.KeyRedo.matchesKey(keyCode, scanCode))
		{
			if(!Screen.hasShiftDown()) redo();
			else redo(true);
			
			callback.setReturnValue(true);
			callback.cancel();
			return;
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
			oldText = new HistoryEntry(getText(), getCursorPosWithOffset(0));
			//obtain last entry
			text = UndoHistory.get(UndoHistory.size() - 1);
			UndoHistory.remove(UndoHistory.size() - 1);
			if(text == null) break;
			
			registerRedo(LastEntry != null ? LastEntry : HistoryEntry.empty());
			LastEntry = text;
			
			//set text
			setText(text.text);
			setCursor(text.cursorPosition);
			if(!oldText.text.startsWith(text.text)) break;
		}
		while(!undoSingle && (UndoHistory.size() > 0 && keepUndoing(text)));
		
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
			oldText = new HistoryEntry(getText(), getCursorPosWithOffset(0));
			//obtain first entry
			text = RedoHistory.get(0);
			RedoHistory.remove(0);
			if(text == null) break;
			
			registerUndo(LastEntry != null ? LastEntry : HistoryEntry.empty());
			LastEntry = text;
			
			//set text
			setText(text.text);
			setCursor(text.cursorPosition);
			if(!text.text.startsWith(oldText.text)) break;
		}
		while(!redoSingle && (RedoHistory.size() > 0 && keepUndoing(text)));
		
		Undoing = false;
	}
	// --------------------------------------------------
	private boolean keepUndoing(HistoryEntry arg0)
	{
		try { return Character.isLetter(arg0.text.charAt(arg0.cursorPosition - 1)); }
		catch(Exception e) { return false; }
	}
	// ==================================================
}
