package thecsdev.uiinputundo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class UIInputUndoClient implements ClientModInitializer
{
	// ==================================================
	public static final KeyBinding KeyUndo = new KeyBinding("thecsdev.uiinputundo.undo_key", InputUtil.GLFW_KEY_Z, "thecsdev.uiinputundo");
	public static final KeyBinding KeyRedo = new KeyBinding("thecsdev.uiinputundo.redo_key", InputUtil.GLFW_KEY_Y, "thecsdev.uiinputundo");
	
	public static final KeyBinding KeyManipReverseText = new KeyBinding("thecsdev.uiinputundo.txtmanip.reversetext", InputUtil.UNKNOWN_KEY.getCode(), "thecsdev.uiinputundo.txtmanip");
	public static final KeyBinding KeyManipReverseWords = new KeyBinding("thecsdev.uiinputundo.txtmanip.reversewords", InputUtil.UNKNOWN_KEY.getCode(), "thecsdev.uiinputundo.txtmanip");
	public static final KeyBinding KeyManipAllUppercase = new KeyBinding("thecsdev.uiinputundo.txtmanip.alluppercase", InputUtil.UNKNOWN_KEY.getCode(), "thecsdev.uiinputundo.txtmanip");
	public static final KeyBinding KeyManipAllLowercase = new KeyBinding("thecsdev.uiinputundo.txtmanip.alllowercase", InputUtil.UNKNOWN_KEY.getCode(), "thecsdev.uiinputundo.txtmanip");
	public static final KeyBinding KeyManipCapitalWords = new KeyBinding("thecsdev.uiinputundo.txtmanip.capitalizeallwords", InputUtil.UNKNOWN_KEY.getCode(), "thecsdev.uiinputundo.txtmanip");
	// ==================================================
	public static final int HistorySize = 50;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//register key bindings
		KeyBindingHelper.registerKeyBinding(KeyUndo);
		KeyBindingHelper.registerKeyBinding(KeyRedo);
		
		KeyBindingHelper.registerKeyBinding(KeyManipReverseText);
		KeyBindingHelper.registerKeyBinding(KeyManipReverseWords);
		KeyBindingHelper.registerKeyBinding(KeyManipAllUppercase);
		KeyBindingHelper.registerKeyBinding(KeyManipAllLowercase);
		KeyBindingHelper.registerKeyBinding(KeyManipCapitalWords);
	}
	// --------------------------------------------------
	public static boolean noAltShift() { return !Screen.hasAltDown() && !Screen.hasShiftDown(); }
	// ==================================================
}
