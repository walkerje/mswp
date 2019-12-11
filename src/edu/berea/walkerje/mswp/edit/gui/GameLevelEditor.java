package edu.berea.walkerje.mswp.edit.gui;


import edu.berea.walkerje.mswp.edit.EToolType;
import edu.berea.walkerje.mswp.edit.TileStamp;
import edu.berea.walkerje.mswp.edit.ToolState;
import edu.berea.walkerje.mswp.gfx.Tile;
import edu.berea.walkerje.mswp.gfx.TileMap;
import edu.berea.walkerje.mswp.play.GameLevel;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

public class GameLevelEditor extends JPanel{
	private static final long serialVersionUID = 3171797799986309521L;

	private GameLevelEditorPanel editorPane;
	private GameLevel level;
	private JScrollPane editorScrollPane;
	private EditorToolDialog toolDialog;
	private JPanel statusInfoPane;
	private JLabel lblMousePosition;
	private JLabel lblMousePositionVal;
	private JLabel lblTilePosition;
	private JLabel lblTilePositionVal;
	private JPanel editorContainerPane;
	private Point hoverPoint = new Point(0,0);
	private JRadioButton rdbtnGrid;
	private JSlider sliderZoom;
	
	/**
	 * This object handles all the mouse events spawned by this editor.
	 */
	private MouseAdapter editorListener = new MouseAdapter() {
		/**
		 * Handles tool performance and rect selection status.
		 */
		public void mouseClicked(MouseEvent e) {
			ToolState ts = toolDialog.getToolState();
			if(ts.getTool() == EToolType.RECT_SELECT) {
				ts.setSelectionBegin(null);
				ts.setSelectionEnd(null);
				toolDialog.rebuildToolPanels();
			}else performToolAt(hoverPoint, false);
		}

		/**
		 * Initiates the current tool, or rectangle selection.
		 */
		public void mousePressed(MouseEvent e) {
			ToolState ts = toolDialog.getToolState();
			if(ts.getTool() == EToolType.RECT_SELECT) {
				ts.setSelectionState(true);
				ts.setSelectionBegin(editorPane.getLevelCoord(e.getPoint().x, e.getPoint().y));
			}else performToolAt(hoverPoint, false);
		}
		
		/**
		 * Handles rectangle selection finalization.
		 * This is where selections are snapped to tile edges, as well.
		 */
		public void mouseReleased(MouseEvent e) {
			ToolState ts = toolDialog.getToolState();
			if(ts.getTool() == EToolType.RECT_SELECT) {
				ts.setSelectionEnd(editorPane.getLevelCoord(e.getPoint().x, e.getPoint().y));
				ts.setSelectionState(false);
				
				if(ts.getSelectionSnapToTile()) {
					Rectangle r = ts.getSelectionBounds();
					//re-shape the selection rectangle to clip to its nearest bounding tiles.
					
					Point beginTile = editorPane.getTileCoord((int)(r.x * editorPane.getScale()), (int)(r.y * editorPane.getScale()));
					beginTile.x *= level.getTilesDrawWidth();
					beginTile.y *= level.getTilesDrawHeight();
					
					Point endTile = editorPane.getTileCoord((int)((r.x + r.width) * editorPane.getScale()), (int)((r.y + r.height) * editorPane.getScale()));
					endTile.x *= level.getTilesDrawWidth();
					endTile.x += level.getTilesDrawWidth();
					
					endTile.y *= level.getTilesDrawHeight();
					endTile.y += level.getTilesDrawHeight();
					
					ts.setSelectionBegin(beginTile);
					ts.setSelectionEnd(endTile);
				}
				toolDialog.rebuildToolPanels();
				editorPane.repaint();
			}
		}
		
		/**
		 * Handles tools and dragging, especially rect selection.
		 */
		public void mouseDragged(MouseEvent e) {
			ToolState ts = toolDialog.getToolState();
			if(ts.getTool() == EToolType.RECT_SELECT) {
				ts.setSelectionEnd(editorPane.getLevelCoord(e.getPoint().x, e.getPoint().y));
				editorPane.repaint();
			}else if(updateMouseInfo(e.getPoint())) {
				performToolAt(hoverPoint, true);
			}
		}

		/**
		 * Updates the mouse information.
		 */
		public void mouseMoved(MouseEvent arg0) {
			updateMouseInfo(arg0.getPoint());
		}
	};
	
	/**
	 * Create the panel.
	 */
	public GameLevelEditor(EditorToolDialog toolDialog, GameLevel level) {
		this.level = level;
		this.toolDialog = toolDialog;
		initComponents();
	}
	
	/**
	 * Initializes the components of this level editor.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		editorPane = new GameLevelEditorPanel(toolDialog.getToolState(), level);
		
		editorPane.addMouseListener(editorListener);
		editorPane.addMouseMotionListener(editorListener);
		
		editorScrollPane = new JScrollPane();
		editorScrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(editorScrollPane, BorderLayout.CENTER);	
		
		editorContainerPane = new JPanel();
		editorScrollPane.setViewportView(editorContainerPane);
		editorContainerPane.setLayout(new BorderLayout(0, 0));
		editorContainerPane.add(editorPane, BorderLayout.CENTER);
		
		statusInfoPane = new JPanel();
		add(statusInfoPane, BorderLayout.SOUTH);
		statusInfoPane.setLayout(new MigLayout("", "[][grow][][grow][]", "[]"));
		
		lblMousePosition = new JLabel("Mouse Position: ");
		statusInfoPane.add(lblMousePosition, "cell 0 0");
		
		lblMousePositionVal = new JLabel("0, 0");
		statusInfoPane.add(lblMousePositionVal, "cell 1 0");
		
		lblTilePosition = new JLabel("Tile Position: ");
		statusInfoPane.add(lblTilePosition, "cell 2 0");
		
		lblTilePositionVal = new JLabel("0, 0");
		statusInfoPane.add(lblTilePositionVal, "cell 3 0");
		
		rdbtnGrid = new JRadioButton("Grid");
		statusInfoPane.add(rdbtnGrid, "flowx,cell 4 0");
		
		UIManager.put("Slider.paintValue", false);
		sliderZoom = new JSlider();
		sliderZoom.setMinimum(50);
		sliderZoom.setMaximum(500);
		sliderZoom.setValue(300);//300% zoom by default.
		sliderZoom.addChangeListener((e)->{
			editorPane.setScale((float)(sliderZoom.getValue() / 100.0f));
			editorPane.revalidate();
			editorPane.repaint();
		});
		statusInfoPane.add(sliderZoom, "cell 4 0");
		
		rdbtnGrid.addChangeListener((e)->{
			editorPane.setShowGrid(rdbtnGrid.isSelected());
		});
	}
	
	/**
	 * Updates the mouse info footer for the editor.
	 * @param mouseP mouse point, relative to the display panel.
	 * @return a boolean indicating if the currently hovered tile has changed.
	 */
	private boolean updateMouseInfo(Point mouseP) {
		lblMousePositionVal.setText(String.format("%d, %d", mouseP.x, mouseP.y));
		Point tileP = editorPane.getTileCoord(mouseP.x, mouseP.y);
		toolDialog.getToolState().setCurrentTileCoord(tileP);
		lblTilePositionVal.setText(String.format("%d, %d", tileP.x, tileP.y));
		
		if(!tileP.equals(hoverPoint)) {
			editorPane.setHoveredTile(tileP);
			editorPane.repaint();
			hoverPoint = tileP;
			return true;
		}
		return false;
	}
	
	/**
	 * Performs the current tool operation at the specified tile point.
	 * @param tilePoint location, in tiles and in the level, the tool is being operated at.
	 * @param isDrag a boolean indicating if we're trying to perform the tool during a mouse drag event.
	 */
	private void performToolAt(Point tilePoint, boolean isDrag) {
		ToolState toolState = toolDialog.getToolState();
		boolean inRange = toolState.isPointInSelection(new Point(tilePoint.x * level.getTilesDrawWidth(), tilePoint.y * level.getTilesDrawHeight())) && level.isValidTile(tilePoint);
		
		if(!inRange)
			return;
		
		if(toolState.getTool() == EToolType.PENCIL) {
			level.getTileLayer(toolState.getCurrentLayer()).set(tilePoint.x, tilePoint.y, toolState.getCurrentTile());
			editorPane.repaintLevelTile(tilePoint);
			editorPane.repaint();
		}else if(toolState.getTool() == EToolType.ERASER) {
			level.getTileLayer(toolState.getCurrentLayer()).set(tilePoint.x, tilePoint.y, null);
			editorPane.repaintLevelTile(tilePoint);
			editorPane.repaint();
		}else if(toolState.getTool() == EToolType.EYEDROP) {
			toolState.setCurrentTile(level.getTileLayer(toolState.getCurrentLayer()).get(tilePoint.x, tilePoint.y));
			SwingUtilities.invokeLater(()->{
				toolDialog.selectTile(toolState.getCurrentTile());
				toolDialog.selectTool(toolState.getPreviousTool());
			});
		}else if(toolState.getTool() == EToolType.FILL && !isDrag) {
			performFillAt(level.getTileLayer(toolState.getCurrentLayer()).get(tilePoint.x, tilePoint.y), toolState.getCurrentTile(), tilePoint.x, tilePoint.y);
			editorPane.repaint();
		}else if(toolState.getTool() == EToolType.STAMP) {
			if(toolState.getCurrentStamp() != null)
				placeStampAt(tilePoint, toolState.getCurrentStamp());
				editorPane.repaint();
		}
	}
	
	/**
	 * Places a stamp at the current hovered tile's position.
	 * @param stamp
	 */
	private void placeStampAt(Point hoverPoint, TileStamp stamp) {
		TileStamp cur = stamp;
		
		Point stampTileOrigin = new Point();
		stampTileOrigin.x = hoverPoint.x - (int)(cur.getExtentWidth() / 2.0f);
		stampTileOrigin.y = hoverPoint.y - (int)(cur.getExtentHeight() / 2.0f);
		
		TileMap layer = level.getTileLayer(toolDialog.getToolState().getCurrentLayer());
		
		for(int tY = 0; tY < stamp.getExtentHeight(); tY++) {
			for(int tX = 0; tX < stamp.getExtentWidth(); tX++) {
				if(cur.get(tX, tY) != null && level.isValidTile(new Point(tX + stampTileOrigin.x, tY + stampTileOrigin.y))) {
					layer.set(tX + stampTileOrigin.x, tY + stampTileOrigin.y, cur.get(tX, tY));
					editorPane.repaintLevelTile(new Point(tX + stampTileOrigin.x, tY + stampTileOrigin.y));
				}
			}
		}
	}
	
	/**
	 * Performs an iterative flood fill.
	 * @param startTile the tile we're trying to replace.
	 * @param destTile the tile we're replacing the start tile with.
	 * @param tX start tile's X tile coordinate.
	 * @param tY start tile's Y tile coordinate.
	 */
	private void performFillAt(Tile startTile, Tile destTile, int tX, int tY) {
		if(startTile == destTile)
			return;
		
		Queue<Point> pointStack = new LinkedList<Point>();
		
		pointStack.add(new Point(tX, tY));
		
		while(!pointStack.isEmpty()) {
			Point ctPos = pointStack.poll();
			
			if(!toolDialog.getToolState().isPointInSelection(new Point(ctPos.x * level.getTilesDrawWidth(), ctPos.y * level.getTilesDrawHeight())))
				continue;
			else if(!level.isValidTile(new Point(ctPos.x, ctPos.y)))
				continue;
			
			if(startTile == level.getTileLayer(toolDialog.getToolState().getCurrentLayer()).get(ctPos.x, ctPos.y)) {
				level.getTileLayer(toolDialog.getToolState().getCurrentLayer()).set(ctPos.x, ctPos.y, destTile);
				editorPane.repaintLevelTile(ctPos);
				
				pointStack.add(new Point(ctPos.x - 1, ctPos.y));
				pointStack.add(new Point(ctPos.x + 1, ctPos.y));
				pointStack.add(new Point(ctPos.x, ctPos.y - 1));
				pointStack.add(new Point(ctPos.x, ctPos.y + 1));
			}
		}
	}
}