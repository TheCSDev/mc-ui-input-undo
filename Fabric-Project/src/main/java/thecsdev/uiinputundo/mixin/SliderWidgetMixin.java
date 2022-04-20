package thecsdev.uiinputundo.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import thecsdev.uiinputundo.client.UIInputUndoClient;

/**
 * It didn't work out, so it wasn't included.
 */
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin
{
	// ==================================================
	public final ArrayList<Double> UndoHistory = new ArrayList<>();
	public final ArrayList<Double> RedoHistory = new ArrayList<>();
	public double LastUndoEntry = 0;
	// ==================================================
	@Inject(method = "<init>", at = @At("RETURN"))
	public void uiinputundo_init(int x, int y, int width, int height, Text text, double value, CallbackInfo callback)
	{
		LastUndoEntry = value;
	}
	
	@Inject(method = "keyPressed", at = @At("RETURN"))
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback)
	{
		if(Screen.hasControlDown()) return;
		if(keyCode == 262 || keyCode == 263)
			registerUndo(getValue());
	}
	
	@Inject(method = "onRelease", at = @At("RETURN"))
	public void onRelease(double mouseX, double mouseY, CallbackInfo callback) { registerUndo(getValue()); }
	// --------------------------------------------------
	@Accessor("value") public abstract double getValue();
	@Invoker("setValue") public abstract void setValue(double value);
	// --------------------------------------------------
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void keyPressedB(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback)
	{
		//check if already undoing or if control is down
		if(!Screen.hasControlDown()) return;
		
		//check for undo
		if(UIInputUndoClient.KeyUndo.matchesKey(keyCode, scanCode))
		{
			undo();
			callback.setReturnValue(true); callback.cancel(); return;
		}
		
		//check for redo
		else if(UIInputUndoClient.KeyRedo.matchesKey(keyCode, scanCode))
		{
			redo();
			callback.setReturnValue(true); callback.cancel(); return;
		}
	}
	// ==================================================
	public void registerUndo(double value)
	{
		//check last entry
		if(UndoHistory.size() > 0)
		{
			double old = UndoHistory.get(UndoHistory.size() - 1);
			if(Math.abs(value-old) <= 0.000001) return;
		}
		
		//add undo
		UndoHistory.add(value);
		
		//limit undo size
		if(UndoHistory.size() > UIInputUndoClient.HistorySize)
			UndoHistory.remove(0);
	}
	
	public void registerRedo(double value)
	{
		//check first entry
		if(RedoHistory.size() > 0)
		{
			double old = RedoHistory.get(0);
			if(Math.abs(value - old) <= 0.000001) return;
		}
		
		//add redo
		RedoHistory.add(0, value);
		
		//limit undo size
		if(RedoHistory.size() > UIInputUndoClient.HistorySize)
			RedoHistory.remove(RedoHistory.size() - 1);
	}
	// --------------------------------------------------
	public void undo()
	{
		//check undo history size
		if(UndoHistory.size() < 1)
			return;
		
		//obtain last entry
		double nextValue = UndoHistory.get(UndoHistory.size() - 1);
		UndoHistory.remove(UndoHistory.size() - 1);
		
		registerRedo(LastUndoEntry);
		LastUndoEntry = nextValue;
		
		//set text
		setValue(nextValue);
	}
	
	public void redo()
	{
		//check redo history size
		if(RedoHistory.size() < 1)
			return;
		
		//obtain first entry
		double nextValue = RedoHistory.get(0);
		RedoHistory.remove(0);
		
		registerUndo(LastUndoEntry);
		LastUndoEntry = nextValue;
		
		//set text
		setValue(nextValue);
	}
	// ==================================================
}
