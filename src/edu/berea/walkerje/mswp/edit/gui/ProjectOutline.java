package edu.berea.walkerje.mswp.edit.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.MouseInputAdapter;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.berea.walkerje.mswp.EAssetType;
import edu.berea.walkerje.mswp.IAsset;
import edu.berea.walkerje.mswp.edit.Editor;
import edu.berea.walkerje.mswp.edit.Project;
import edu.berea.walkerje.mswp.edit.TileStamp;
import edu.berea.walkerje.mswp.gfx.Spritesheet;
import edu.berea.walkerje.mswp.gfx.Tilesheet;
import edu.berea.walkerje.mswp.play.GameLevel;

public class ProjectOutline extends JPanel {
	private static final long serialVersionUID = -703501786265571628L;

	private Project workingProject;
	private JScrollPane contentPane;
	private JTree outlineTree;
	
	private String selectionPathStr = null;
	
	private JPanel selectionPane;
	
	private Editor editor;
	
	/**
	 * Create the panel.
	 */
	public ProjectOutline(Editor editor, Project p) {
		this.editor = editor;
		workingProject = p;
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		JScrollPane treePane = new JScrollPane();
		splitPane.setLeftComponent(treePane);
		
		outlineTree = new JTree();
		outlineTree.addMouseListener(new MouseInputAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath path = outlineTree.getPathForLocation(e.getX(), e.getY());
				System.out.println(e.getClickCount());
				if(path != null){
				if(path.getPathCount() <= 2)
					return;//Again, working on the assumption that we only select paths with a length > 2.
				
				EAssetType selectionType = (EAssetType)(((DefaultMutableTreeNode)path.getParentPath().getLastPathComponent()).getUserObject());
				String selectionName = ((DefaultMutableTreeNode)(path.getLastPathComponent())).getUserObject().toString();

				//What a ridiculous series of function calls to get what I originally put in the tree...
				IAsset asset = workingProject.getAsset(selectionType, selectionName);
				if(asset == null)
					return;
				
				if(selectionPathStr == null ? false : selectionPathStr.equals(path.toString())){
					if (e.getClickCount() == 2){
						if (asset instanceof GameLevel){
							editor.openLevelEditor((GameLevel)asset);
						}
					}
					return;//Already showing the view for that specific asset! :(
				}
				
				selectionPathStr = path.toString();
				
				if(asset instanceof Tilesheet) {
					swapViewPane(new TilesheetAssetView((Tilesheet)asset));
				}else if(asset instanceof Spritesheet) {
					swapViewPane(new SpritesheetAssetView((Spritesheet)asset));
				}else if(asset instanceof GameLevel) {
					swapViewPane(new GameLevelAssetView(editor, (GameLevel)asset));
				}else if(asset instanceof TileStamp) {
					swapViewPane(new TileStampAssetView(editor, (TileStamp)asset));
				}
			}
			}
		});
		outlineTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		outlineTree.setRootVisible(false);
		treePane.setViewportView(outlineTree);
		
		contentPane = new JScrollPane();
		splitPane.setRightComponent(contentPane);
		
		contentPane.setViewportView(new JPanel());
		rebuildTreeModel();

		SwingUtilities.invokeLater(()->{
			splitPane.setResizeWeight(0.5);
			splitPane.setDividerLocation(0.4);
			splitPane.setOneTouchExpandable(true);
		});
	}
	
	/**
	 * Rebuilds the asset tree's model. Maintains the current selection, if it can.
	 */
	public void rebuildTreeModel() {
		//We can assume that a selection path will NEVER have a depth greater than 3 (root/asset_type/asset_name)
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		HashMap<String, IAsset>[] assets = workingProject.getAssets();
		
		TreePath restoredPath = null;
		
		for(EAssetType assetType : EAssetType.values()) {
			DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(assetType);
			
			
			HashMap<String, IAsset> assetMap = assets[assetType.ordinal()];
			
			for(HashMap.Entry<String, IAsset> entry : assetMap.entrySet()){
				DefaultMutableTreeNode assetNode = new DefaultMutableTreeNode(entry.getKey());
				
				Object[] testPathArr = {root, typeNode, assetNode};
				TreePath testPath = new TreePath(testPathArr);
				
				//Naively test their strings. Not the best way to check for this, but it works
				if(testPath.toString().equals(selectionPathStr))
					restoredPath = testPath;
				
				typeNode.add(assetNode);
			}
			root.add(typeNode);
		}
		
		outlineTree.setModel(new DefaultTreeModel(root));
		if(restoredPath != null)
			outlineTree.setSelectionPath(restoredPath);
		else selectionPathStr = null;//Previous selection just must not exist, then.
	}

	/**
	 * Swaps the current visible asset view for the specified panel.
	 * @param p
	 */
	public void swapViewPane(JPanel p) {
		if(selectionPane != null) {
			if(selectionPane instanceof SpritesheetAssetView) {
				((SpritesheetAssetView)selectionPane).stopUpdatingSprites();
			}
		}
		
		selectionPane = p;
		contentPane.setViewportView(p);
		contentPane.revalidate();
		contentPane.repaint();//may not be needed, but just in case~
	}
}