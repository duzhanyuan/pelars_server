package pelarsServer;

public class InterfaceC {
	
	static {
		System.loadLibrary("hello");
	}
	
	public native void sayHello();
	
	public native int sumHello(int x, int y);

}
