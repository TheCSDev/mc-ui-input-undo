package thecsdev.uiinputundo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class UIInputUndoClient implements ClientModInitializer
{
	// ==================================================
	public static final KeyBinding KeyUndo = new KeyBinding("thecsdev.uiinputundo.undo_key", InputUtil.GLFW_KEY_Z, "thecsdev.uiinputundo");
	public static final KeyBinding KeyRedo = new KeyBinding("thecsdev.uiinputundo.redo_key", InputUtil.GLFW_KEY_Y, "thecsdev.uiinputundo");
	// ==================================================
	public static final int HistorySize = 50;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//register key bindings
		KeyBindingHelper.registerKeyBinding(KeyUndo);
		KeyBindingHelper.registerKeyBinding(KeyRedo);
	}
	// ==================================================
}
