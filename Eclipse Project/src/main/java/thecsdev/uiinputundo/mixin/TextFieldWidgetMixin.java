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
import thecsdev.uiinputundo.client.UIInputUndoClient;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin
{
	// ==================================================
	public final ArrayList<String> UndoHistory = new ArrayList<>();
	public final ArrayList<String> RedoHistory = new ArrayList<>();
	public String LastEntry = null;
	private boolean Undoing = false;
	// ==================================================
	@Inject(at = @At("TAIL"), method = "onChanged")
	public void onChanged(String newText, CallbackInfo callback)
	{
		//avoid null newText and registering undo when undoing/redoing
		if(newText == null || Undoing) return;
		//avoid registering undo same texts
		else if(StringUtils.equals(LastEntry, newText)) return;
				
		//handle last entry
		if(StringUtils.isEmpty(LastEntry))
		{
			if(UndoHistory.size() == 0) UndoHistory.add("");
			LastEntry = newText;
		}
		
		//register undo and clear redo
		registerUndo(LastEntry);
		LastEntry = newText;
		RedoHistory.clear();
	}
	// --------------------------------------------------
	@Accessor("text") public abstract String getText();
	@Invoker("setText") public abstract void setText(String text);
	
	@Invoker("isActive")       public abstract boolean isActive();
	@Invoker("setCursorToEnd") public abstract void setCursorToEnd();
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
	public void registerUndo(String text)
	{
		//check last entry
		if(UndoHistory.size() > 0 && UndoHistory.get(UndoHistory.size() - 1).equals(text))
			return;
		
		//add undo
		UndoHistory.add(text);
		
		//limit undo size
		if(UndoHistory.size() > UIInputUndoClient.HistorySize)
			UndoHistory.remove(0);
	}
	
	public void registerRedo(String text)
	{
		//check first entry
		if(RedoHistory.size() > 0 && RedoHistory.get(0).equals(text))
			return;
		
		//add redo
		RedoHistory.add(0, text);
		
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
		
		String oldText = null, text = null;
		do
		{
			oldText = getText();
			//obtain last entry
			text = UndoHistory.get(UndoHistory.size() - 1);
			UndoHistory.remove(UndoHistory.size() - 1);
			if(text == null) break;
			
			registerRedo(LastEntry != null ? LastEntry : "");
			LastEntry = text;
			
			//set text
			setText(text);
			if(!oldText.startsWith(text)) break;
		}
		while(!undoSingle && (UndoHistory.size() > 0 && keepUndoing(text.charAt(text.length() - 1))));
		
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
		
		String oldText = null, text = null;
		do
		{
			oldText = getText();
			//obtain first entry
			text = RedoHistory.get(0);
			RedoHistory.remove(0);
			if(text == null) break;
			
			registerUndo(LastEntry != null ? LastEntry : "");
			LastEntry = text;
			
			//set text
			setText(text);
			if(!text.startsWith(oldText)) break;
		}
		while(!redoSingle && (RedoHistory.size() > 0 && keepUndoing(text.charAt(text.length() - 1))));
		
		Undoing = false;
	}
	// --------------------------------------------------
	private boolean keepUndoing(char arg0) { return Character.isLetter(arg0); }
	// ==================================================
}
