package thecsdev.uiinputundo.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod(value = UIInputUndoClient.ModID)
public class UIInputUndoClient
{
	// ==================================================
	public static final String ModID = "uiinputundo";
	
	public static final KeyMapping KeyUndo = new KeyMapping("thecsdev.uiinputundo.undo_key", 90, "thecsdev.uiinputundo");
	public static final KeyMapping KeyRedo = new KeyMapping("thecsdev.uiinputundo.redo_key", 89, "thecsdev.uiinputundo");
	
	public static final KeyMapping KeyManipReverseText = new KeyMapping("thecsdev.uiinputundo.txtmanip.reversetext", -1, "thecsdev.uiinputundo.txtmanip");
	public static final KeyMapping KeyManipReverseWords = new KeyMapping("thecsdev.uiinputundo.txtmanip.reversewords", -1, "thecsdev.uiinputundo.txtmanip");
	public static final KeyMapping KeyManipAllUppercase = new KeyMapping("thecsdev.uiinputundo.txtmanip.alluppercase", -1, "thecsdev.uiinputundo.txtmanip");
	public static final KeyMapping KeyManipAllLowercase = new KeyMapping("thecsdev.uiinputundo.txtmanip.alllowercase", -1, "thecsdev.uiinputundo.txtmanip");
	public static final KeyMapping KeyManipCapitalWords = new KeyMapping("thecsdev.uiinputundo.txtmanip.capitalizeallwords", -1, "thecsdev.uiinputundo.txtmanip");
	// ==================================================
	public static final int HistorySize = 50;
	// ==================================================
	public UIInputUndoClient()
	{
		ClientRegistry.registerKeyBinding(KeyUndo);
		ClientRegistry.registerKeyBinding(KeyRedo);
		
		ClientRegistry.registerKeyBinding(KeyManipReverseText);
		ClientRegistry.registerKeyBinding(KeyManipReverseWords);
		ClientRegistry.registerKeyBinding(KeyManipAllUppercase);
		ClientRegistry.registerKeyBinding(KeyManipAllLowercase);
		ClientRegistry.registerKeyBinding(KeyManipCapitalWords);
	}
	// --------------------------------------------------
	public static boolean noAltShift() { return !Screen.hasAltDown() && !Screen.hasShiftDown(); }
	// ==================================================
}
