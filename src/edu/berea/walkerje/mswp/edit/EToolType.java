package edu.berea.walkerje.mswp.edit;

public enum EToolType {
	// Tool names and images are directly pulled from this enum
	PENCIL			("Pencil", "/edu/berea/walkerje/mswp/asset/pencil.png"),
	ERASER			("Eraser", "/edu/berea/walkerje/mswp/asset/eraser.png"),
	FILL			("Fill", "/edu/berea/walkerje/mswp/asset/paintbucket.png"),
	RECT_SELECT		("Select", "/edu/berea/walkerje/mswp/asset/rectselect.png"),
	STAMP			("Stamp", "/edu/berea/walkerje/mswp/asset/stamp.png"),
	EYEDROP			("Eyedropper", "/edu/berea/walkerje/mswp/asset/dropper.png"),
	PUT_SPRITE		("Put Sprite", "/edu/berea/walkerje/mswp/asset/sprite.png");
	
	/*Display Name String*/
	public final String displayName;
	public final String imagePath;
	
	EToolType(String displayName, String imagePath){
		this.displayName = displayName;
		this.imagePath = imagePath;
	}
}