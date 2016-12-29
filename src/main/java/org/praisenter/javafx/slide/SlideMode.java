package org.praisenter.javafx.slide;

public enum SlideMode {
	/** For editing a slide */
	EDIT,
	
	/** For taking a snapshot of a slide (EDIT w/o scaling and selection borders) */
	SNAPSHOT,
	
	/** For previewing a slide (PRESENT w/ audio muted by default) */
	PREVIEW,
		
	/** Normal presentation */
	PRESENT,

	/** TODO not sure how this will be used just yet */
	MUSICIAN
}
