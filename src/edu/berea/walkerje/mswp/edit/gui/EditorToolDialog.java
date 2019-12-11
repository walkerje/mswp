package edu.berea.walkerje.mswp.edit.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import edu.berea.walkerje.mswp.EAssetType;
import edu.berea.walkerje.mswp.IAsset;
import edu.berea.walkerje.mswp.IRegion;
import edu.berea.walkerje.mswp.edit.EToolType;
import edu.berea.walkerje.mswp.edit.Editor;
import edu.berea.walkerje.mswp.edit.Project;
import edu.berea.walkerje.mswp.edit.TileStamp;
import edu.berea.walkerje.mswp.edit.ToolState;
import edu.berea.walkerje.mswp.edit.gui.modal.TileStampAddDialog;
import edu.berea.walkerje.mswp.gfx.Tile;
import edu.berea.walkerje.mswp.gfx.TileMap;
import edu.berea.walkerje.mswp.gfx.Tilesheet;
import edu.berea.walkerje.mswp.play.GameLevel;
import net.miginfocom.swing.MigLayout;

public class EditorToolDialog extends JDialog {
	private static final long serialVersionUID = 4264144893403174803L;
	
	private Editor editor;
	private Project project;
	
	private JPanel contentPanel = new JPanel();
	private JPanel toolButtonPanel;
	
	private JToggleButton[] toolButtons;
	
	private JPanel selectionPanel;
	private JComboBox<String> layerComboBox;
	private JPanel toolInfoPanel;
	private JPanel tileToolPanel;
	private JPanel eraseToolPanel;
	private JPanel rectSelectToolPanel;
	private JPanel stampToolPanel;
	private JComboBox<String> tilesheetComboBox;
	private JScrollPane tilesheetScrollPanel;
	private JPanel tilesheetSelectionPanel;
	private JPanel tilesheetViewPanel;
	
	private SpriteView tileSelectionView = new SpriteView(null);

	private ToolState toolState = new ToolState(EToolType.PENCIL);
	private JLabel lblTilesheetNameVal;
	private JLabel lblTileInfoVal;
	private JRadioButton rdbtnSnapToTile;
	private JRadioButton rdbtnSelectEntities;
	private JLabel lblSelectionSize;
	private JLabel lblSelectionSizeVal;
	private JLabel lblSelectionPosition;
	private JLabel lblSelectionPositionVal;
	private JButton btnCreateStamp;
	private JSplitPane stampToolSplitPanel;
	private JScrollPane stampToolListScrollPanel;
	private JPanel stampToolListPane;
	private JPanel stampPreviewPanel;
	private JSlider stampPreviewScaleSlider;
	private JScrollPane stampPreviewScrollPanel;
	private JPanel stampPreviewContentPanel;
	
	private TileStampView stampPreview;
	
	/**
	 * Tool change listener handles tool button presses.
	 * It untoggles all other tool buttons.
	 */
	private class ToolChangeListener implements ActionListener{
		private final EToolType tool;
		
		public ToolChangeListener(EToolType tType) {
			tool = tType;
		}

		public void actionPerformed(ActionEvent e) {
			if(toolButtons == null)
				return;
			selectTool(tool);
		}
	}
	
	/**
	 * This listener handles the selection of a current stamp.
	 */
	private class StampSelectListener extends MouseAdapter{
		
		private TileStamp stamp;
		
		public StampSelectListener(TileStamp st) {
			this.stamp = st;
		}
		
		public void mouseClicked(MouseEvent arg0) {
			SwingUtilities.invokeLater(()->{
				toolState.setCurrentStamp(stamp);
				rebuildToolPanels();
			});
		}
	}
	
	/**
	 * This listens for a press for tile sprite views. Used to change the currently selected tile.
	 */
	private class TileSelectListener extends MouseAdapter{
		private Tile tile;
		
		public TileSelectListener(Tile tile) {
			this.tile = tile;
		}
		
		public void mouseClicked(MouseEvent arg0) {
			SwingUtilities.invokeLater(()->{
				selectTile(tile);
			});
		}
	}
	
	/**
	 * Create the dialog.
	 */
	public EditorToolDialog(Editor fr, Project proj) {
		super(fr);
		this.editor = fr;
		this.project = proj;
		setTitle("Tools");
		initComponents();
		setVisible(true);
	}
	
	/**
	 * Initializes all components in this editor tool dialog.
	 */
	private void initComponents() {
		setBounds(100, 100, 350, 502);
		setContentPane(contentPanel);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		selectionPanel = new JPanel();
		contentPanel.add(selectionPanel, BorderLayout.NORTH);
		selectionPanel.setLayout(new BorderLayout(0, 0));
		
		toolButtonPanel = new JPanel();
		selectionPanel.add(toolButtonPanel, BorderLayout.NORTH);
		toolButtonPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 5, 5));

		// In order to prevent fragile code from misordering tools, generate tools directly from enum
		// TODO: Add proper handling for when a tool's image cannot be loaded
		// it is currently not convinent to debug, as image names are stored in the enumeration.
		toolButtons = new JToggleButton[EToolType.values().length];
		int toolNum = 0;
		for(EToolType type : EToolType.values()){
			JToggleButton tglbtn = new JToggleButton("");
			tglbtn.setIcon(new ImageIcon(EditorToolDialog.class.getResource(type.imagePath)));
			tglbtn.addActionListener(new ToolChangeListener(type));
			toolButtonPanel.add(tglbtn);
			toolButtons[toolNum++] = tglbtn;
		}
		toolButtons[0].setSelected(true); // Autoselect first button
		
		layerComboBox = new JComboBox<String>();
		layerComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Background", "Midground", "Foreground"}));
		layerComboBox.setSelectedIndex(toolState.getCurrentLayer());
		layerComboBox.addActionListener((e)->{
			toolState.setCurrentLayer(layerComboBox.getSelectedIndex());
			rebuildToolPanels();
		});
		selectionPanel.add(layerComboBox, BorderLayout.CENTER);
		
		toolInfoPanel = new JPanel();
		toolInfoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPanel.add(toolInfoPanel, BorderLayout.CENTER);
		toolInfoPanel.setLayout(new CardLayout(0, 0));
		
		{
			tileToolPanel = new JPanel();
			toolInfoPanel.add(tileToolPanel, "tile");
			tileToolPanel.setLayout(new BorderLayout(0, 0));
			
			{
				tilesheetComboBox = new JComboBox<String>();
				tilesheetComboBox.addActionListener((e)->{
					if(!project.getAssets()[EAssetType.TILESHEET.ordinal()].isEmpty())
						toolState.setCurrentTilesheet((Tilesheet)project.getAsset(EAssetType.TILESHEET, (String)tilesheetComboBox.getSelectedItem()));
					else toolState.setCurrentTilesheet(null);
					rebuildToolPanels();
				});
				
				tileToolPanel.add(tilesheetComboBox, BorderLayout.NORTH);
				
				tilesheetScrollPanel = new JScrollPane();
				tileToolPanel.add(tilesheetScrollPanel, BorderLayout.CENTER);
				
				tilesheetViewPanel = new JPanel();
				tilesheetScrollPanel.setViewportView(tilesheetViewPanel);
				tilesheetViewPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
				
				tilesheetSelectionPanel = new JPanel();
				tileToolPanel.add(tilesheetSelectionPanel, BorderLayout.SOUTH);
				tilesheetSelectionPanel.setLayout(new MigLayout("", "[][grow][]", "[]"));
				
				tilesheetSelectionPanel.add(tileSelectionView, "cell 0 0, alignx center");
				
				lblTilesheetNameVal = new JLabel("No Selection");
				tilesheetSelectionPanel.add(lblTilesheetNameVal, "cell 1 0,alignx right");
				
				lblTileInfoVal = new JLabel("");
				tilesheetSelectionPanel.add(lblTileInfoVal, "cell 2 0");
			}
		}
		
		eraseToolPanel = new JPanel();
		toolInfoPanel.add(eraseToolPanel, "erase");
		
		rectSelectToolPanel = new JPanel();
		toolInfoPanel.add(rectSelectToolPanel, "select");
		rectSelectToolPanel.setLayout(new MigLayout("", "[][grow][]", "[][][]"));
		
		rdbtnSnapToTile = new JRadioButton("Snap to Tile");
		rdbtnSnapToTile.addChangeListener((e)->{
			toolState.setSelectionSnapToTile(rdbtnSnapToTile.isSelected());
		});
		toolState.setSelectionSnapToTile(rdbtnSnapToTile.isSelected());
		rectSelectToolPanel.add(rdbtnSnapToTile, "cell 0 0,alignx left");

		lblSelectionPosition = new JLabel("Selection Position: ");
		rectSelectToolPanel.add(lblSelectionPosition, "cell 1 0,alignx right");
		
		lblSelectionPositionVal = new JLabel("");
		rectSelectToolPanel.add(lblSelectionPositionVal, "cell 2 0");
		
		rdbtnSelectEntities = new JRadioButton("Select Entities");
		rectSelectToolPanel.add(rdbtnSelectEntities, "cell 0 1,alignx left");
		
		lblSelectionSize = new JLabel("Selection Size: ");
		rectSelectToolPanel.add(lblSelectionSize, "cell 1 1,alignx right");
		
		lblSelectionSizeVal = new JLabel("");
		rectSelectToolPanel.add(lblSelectionSizeVal, "cell 2 1");
		
		btnCreateStamp = new JButton("Create Stamp");
		btnCreateStamp.addActionListener((e)->{
			final Rectangle worldSel = toolState.getSelectionBounds();
			Rectangle sel = worldSel == null ? null : new Rectangle(worldSel);
			GameLevel curLevel = toolState.getCurrentLevel();
			if(sel != null) {
				if(sel.x % curLevel.getTilesDrawWidth() != 0 || sel.y % curLevel.getTilesDrawHeight() != 0 ||
				   sel.width % curLevel.getTilesDrawWidth() != 0 || sel.height % curLevel.getTilesDrawHeight() != 0) {
					JOptionPane.showMessageDialog(this, "Must snap selection to tiles when creating a stamp!", "Could Not Create", JOptionPane.WARNING_MESSAGE);
					return;
				}
				GameLevel level = toolState.getCurrentLevel();
				TileMap layer = level.getTileLayer(toolState.getCurrentLayer());
				
				//transform the selection to tile space.
				sel.x /= level.getTilesDrawWidth();
				sel.y /= level.getTilesDrawHeight();
				sel.width /= level.getTilesDrawWidth();
				sel.height /= level.getTilesDrawHeight();
				
				TileStamp tempStamp = TileStamp.extract("", layer, IRegion.wrap(sel));
				TileStampAddDialog d = new TileStampAddDialog(this, tempStamp);
				d.setVisible(true);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(!d.isVisible()) {
							if(d.getResult() != null) {
								TileStamp result = d.getResult();
								toolState.setSelectionBegin(null);
								toolState.setSelectionEnd(null);
								project.addAsset(result);
								editor.rebuildOutline();
								rebuildToolPanels();
							}
						}else SwingUtilities.invokeLater(this);
					}
				});
			}else JOptionPane.showMessageDialog(this, "No selection to create a stamp with!", "Could not create tile stamp", JOptionPane.WARNING_MESSAGE);
		});
		rectSelectToolPanel.add(btnCreateStamp, "cell 0 2,alignx left");
		
		stampToolPanel = new JPanel();
		toolInfoPanel.add(stampToolPanel, "stamp");
		stampToolPanel.setLayout(new BorderLayout(0, 0));
		
		stampToolSplitPanel = new JSplitPane();
		stampToolSplitPanel.setResizeWeight(0.65);
		stampToolSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		stampToolPanel.add(stampToolSplitPanel, BorderLayout.CENTER);
		
		stampToolListScrollPanel = new JScrollPane();
		stampToolSplitPanel.setLeftComponent(stampToolListScrollPanel);
		
		stampToolListPane = new JPanel();
		stampToolListPane.setLayout(new WrapLayout(FlowLayout.CENTER));
		stampToolListScrollPanel.setViewportView(stampToolListPane);
		
		stampPreviewPanel = new JPanel();
		stampToolSplitPanel.setRightComponent(stampPreviewPanel);
		stampPreviewPanel.setLayout(new BorderLayout(0, 0));
		
		stampPreviewScaleSlider = new JSlider();
		stampPreviewScaleSlider.setMinimum(50);
		stampPreviewScaleSlider.setMaximum(500);
		stampPreviewScaleSlider.setValue(100);
		stampPreviewScaleSlider.addChangeListener((e)->{
			if(stampPreview != null) {
				stampPreview.setScale(stampPreviewScaleSlider.getValue() / 100.0f);
				stampPreview.revalidate();
				stampPreview.repaint();
			}
		});
		stampPreviewPanel.add(stampPreviewScaleSlider, BorderLayout.SOUTH);
		
		stampPreviewScrollPanel = new JScrollPane();
		stampPreviewPanel.add(stampPreviewScrollPanel, BorderLayout.CENTER);
		
		stampPreviewContentPanel = new JPanel();
		stampPreviewScrollPanel.setViewportView(stampPreviewContentPanel);
		
		rebuildToolPanels();
	}
	
	/**
	 * Selects the specified tool.
	 * Swaps tool panels when necessary, and deactivates all other tool toggle buttons.
	 * @param t
	 */
	public void selectTool(EToolType t) {
		//Pencil, Eraser, fill, rect select, stamp, eyedrop, put_sprite
		if(toolButtons == null)
			return;
		
		toolState.setTool(t);
		
		for(int i = 0; i < toolButtons.length; i++) {
			JToggleButton btn = toolButtons[i];
			if(i != t.ordinal())
				btn.setSelected(false);
			else if(!btn.isSelected())
				btn.setSelected(true);
			else swapToolPane(t);
		}
	}
	
	
	/**
	 * Rebuilds all of the tool panels (e.g, fills out their individual parts that hold information relative to something non-gui, like the editor's state.
	 */
	public void rebuildToolPanels() {
		layerComboBox.setSelectedIndex(toolState.getCurrentLayer());
		
		{//Rebuild Tile Panel
			DefaultComboBoxModel<String> tsBoxModel = new DefaultComboBoxModel<String>();
			
			int curIndex = 0;
			int selectionIndex = -1;
			Map<String, IAsset> tilesheets = project.getAssets()[EAssetType.TILESHEET.ordinal()];
			Iterator<Map.Entry<String, IAsset>> tsIter = tilesheets.entrySet().iterator();
			
			while(tsIter.hasNext()) {
				Map.Entry<String, IAsset> entry = tsIter.next();
				tsBoxModel.addElement(entry.getKey());
				if(entry.getValue() == toolState.getCurrentTilesheet()){
					selectionIndex = curIndex;
				}
				curIndex++;
			}
			
			tilesheetComboBox.setModel(tsBoxModel);
			
			if(selectionIndex > -1)
				tilesheetComboBox.setSelectedIndex(selectionIndex);
			else if(tsBoxModel.getSize() > 0) {
				toolState.setCurrentTilesheet((Tilesheet)tilesheets.entrySet().iterator().next().getValue());
				tilesheetComboBox.setSelectedIndex(0);
			}
			
			tilesheetViewPanel.removeAll();
			
			if(toolState.getCurrentTilesheet() != null) {
				Tilesheet ts = toolState.getCurrentTilesheet();
				
				for(int y = 0; y < ts.getTilesY(); y++) {
					for(int x = 0; x < ts.getTilesX(); x++) {
						Tile tile = ts.get(x, y);
						SpriteView view = new SpriteView(tile.getSpriteProvider().getNextSprite());
						view.addMouseListener(new TileSelectListener(tile));
						if(new Point(x,y).equals(toolState.getCurrentTileCoord()) && tile.getTilesheet() == toolState.getCurrentTilesheet()) {	
							//Update our selection text while we're at it...
							tileSelectionView.setProvider(tile.getSpriteProvider());
							tileSelectionView.revalidate();
							tileSelectionView.repaint();
							lblTileInfoVal.setText(String.format("at %d, %d", x, y));
							lblTileInfoVal.repaint();
							
							view.setForeground(new Color(0,255,0,128));
						}
						tilesheetViewPanel.add(view);
					}
				}
			}
		}
		
		{//Rebuild selection panel info.
			Rectangle selectRect = toolState.getSelectionBounds();
			if(selectRect != null) {
				lblSelectionPositionVal.setText(String.format("%d, %d", selectRect.x, selectRect.y));
				lblSelectionSizeVal.setText(String.format("%d x %d", selectRect.width, selectRect.height));
			}else {
				lblSelectionPositionVal.setText("N/A");
				lblSelectionSizeVal.setText("N/A");
			}
		}
		
		{//Rebuild Stamp panel info.
			stampToolListPane.removeAll(); //this is the wrap-layout list panel that holds all of our "stamps".
			
			project.getAssets()[EAssetType.TILE_STAMP.ordinal()].forEach((k,v)->{
				TileStamp stamp = (TileStamp)v;
				TileStampView view = new TileStampView(stamp);
				view.addMouseListener(new StampSelectListener(stamp));
				if(stamp == toolState.getCurrentStamp())
					view.setForeground(Color.GREEN);
				stampToolListPane.add(view);
			});
			
			if(stampPreview == null && toolState.getCurrentStamp() != null) {
				stampPreview = new TileStampView(toolState.getCurrentStamp());
				stampPreview.setScale(stampPreviewScaleSlider.getValue() / 100.0f);
				stampPreviewContentPanel.add(stampPreview);
			}else if(stampPreview != null ? stampPreview.getStamp() != null : false){
				stampPreview.setStamp(toolState.getCurrentStamp());
				stampPreview.revalidate();
				stampPreview.repaint();
			}
		}
	}
	
	/**
	 * Changes the currently selected tile.
	 * @param t
	 */
	public void selectTile(Tile t) {
		final Point tilePt = t.getSourceTileCoord();
		toolState.setCurrentTile(t);
		toolState.setCurrentTileCoord(tilePt);
		tileSelectionView.setProvider(t.getSpriteProvider());
		tileSelectionView.revalidate();
		tileSelectionView.repaint();
		lblTileInfoVal.setText(String.format("at %d, %d", tilePt.x, tilePt.y));
		lblTileInfoVal.repaint();
		lblTilesheetNameVal.setText(t.getTilesheet().getAssetName());
		rebuildToolPanels();
	}
	
	/**
	 * Swaps the lower pane in the dialog for one appropriate for the specified tool's type.
	 * @param type
	 */
	private void swapToolPane(EToolType type) {
		CardLayout cards = (CardLayout)toolInfoPanel.getLayout();
		
		String dest = null;
		//TODO: This could be handled more elegantly, but a jump table isn't too bad.
		switch(type) {
		case FILL:
		case PENCIL:
		case ERASER:
		case EYEDROP:
			dest = "tile";
			break;
		case RECT_SELECT:
			dest = "select";
			break;
		case STAMP:
			dest = "stamp";
		default:
			break;
		}
		
		if(dest != null) {
			cards.show(toolInfoPanel, dest);
		}
	}

	/**
	 * @return the tool state of this dialog.
	 */
	public ToolState getToolState() {
		return toolState;
	}
}
