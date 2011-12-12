import org.lwjgl.input.Keyboard;

public class mod_ThatFreezesTheGame extends BaseMod {

	@Override
	public void load() {
		ModLoader.RegisterKey(this, new aby("Test", Keyboard.KEY_R), false);
	}
	
	public void KeyboardEvent(aby keyBinding) {
		System.out.println("Hello");
	}

	@Override
	public String getVersion() {
		return "1";
	}


}
