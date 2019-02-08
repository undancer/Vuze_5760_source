/*
 * Created on 19 nov. 2004
 * Created by Olivier Chalouhi
 * 
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.gudy.azureus2.ui.swt.views;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerPeerListener;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.peer.PEPeerStats;
import org.gudy.azureus2.core3.peer.impl.PEPeerTransport;
import org.gudy.azureus2.core3.peer.util.PeerUtils;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.plugins.ui.UIPluginViewToolBarListener;
import org.gudy.azureus2.ui.swt.*;
import org.gudy.azureus2.ui.swt.components.graphics.PieUtils;
import org.gudy.azureus2.ui.swt.mainwindow.Colors;
import org.gudy.azureus2.ui.swt.plugins.UISWTView;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;
import org.gudy.azureus2.ui.swt.pluginsimpl.UISWTViewCore;
import org.gudy.azureus2.ui.swt.pluginsimpl.UISWTViewCoreEventListener;
import org.gudy.azureus2.ui.swt.pluginsimpl.UISWTViewCoreEventListenerEx;
import org.gudy.azureus2.ui.swt.pluginsimpl.UISWTViewEventListenerHolder;

import com.aelitis.azureus.core.networkmanager.admin.NetworkAdmin;
import com.aelitis.azureus.ui.UIFunctionsManager;
import com.aelitis.azureus.ui.common.ToolBarItem;
import com.aelitis.azureus.ui.mdi.MdiEntry;
import com.aelitis.azureus.ui.selectedcontent.SelectedContent;
import com.aelitis.azureus.ui.selectedcontent.SelectedContentManager;

/**
 * This is the "Swarm" View
 * 
 * @author Olivier Chalouhi
 *
 */
public class PeersGraphicView
	implements UISWTViewCoreEventListener, UIPluginViewToolBarListener, UISWTViewCoreEventListenerEx
{
  
  public static String MSGID_PREFIX = "PeersGraphicView";

  private UISWTView swtView;
  private static final int PEER_SIZE = 18;
  //private static final int PACKET_SIZE = 10;
  private static final int OWN_SIZE_DEFAULT = 75;
  private static final int OWN_SIZE_MIN		= 30;
  private static final int OWN_SIZE_MAX 	= 75;
  private static int OWN_SIZE = OWN_SIZE_DEFAULT;
  
  private static final int NB_ANGLES = 1000;  
  //private double[] deltaPerimeters;
  private double perimeter;
  private double[] rs;
  
  private final double[] angles;
  private final double[] deltaXXs;
  private final double[] deltaXYs;
  private final double[] deltaYXs;
  private final double[] deltaYYs;
  
  //Comparator Class
  //Note: this comparator imposes orderings that are inconsistent with equals.
  private static class 
  PeerComparator 
  	implements Comparator<PEPeer> 
  {
  	public int compare(PEPeer peer0, PEPeer peer1) {

      int percent0 = peer0.getPercentDoneInThousandNotation();
      int percent1 = peer1.getPercentDoneInThousandNotation();
      
      int result = percent0 - percent1;
      
      if ( result == 0 ){
    	  
    	  long l = peer0.getTimeSinceConnectionEstablished() - peer1.getTimeSinceConnectionEstablished();
    	  
    	  if ( l < 0 ){
    		  result = -1;
    	  }else if ( l > 0 ){
    		  result = 1;
    	  }
      }
      
      return( result );
    }
  }
  
  private PeerComparator peerComparator;
  
  
  private Image					my_flag;
  
  //UI Stuff
  private Display 		display;
  private Composite 	panel;

  
  private static class
  ManagerData
  	implements DownloadManagerPeerListener
  {
	  private AEMonitor peers_mon = new AEMonitor( "PeersGraphicView:peers" );;

	  private DownloadManager		manager;
	  
	  private Point 				oldSize; 
	  private List<PEPeer> 			peers;
	  private Map<PEPeer,int[]>		peer_hit_map = new HashMap<PEPeer, int[]>();
	  private int					me_hit_x;
	  private int					me_hit_y;
	  
	  private
	  ManagerData(
		 DownloadManager	_manager )
	  {
		  manager	= _manager;
		  
		  peers = new ArrayList<PEPeer>();
		  
		  manager.addPeerListener(this);
	  }
	  
	  private void
	  delete()
	  {
		  manager.removePeerListener(this);

		  peer_hit_map.clear();
	  }
	  
	  public void peerManagerWillBeAdded( PEPeerManager	peer_manager ){}
	  public void peerManagerAdded(PEPeerManager manager) {}
	  public void peerManagerRemoved(PEPeerManager manager) {}
	  
	  public void peerAdded(PEPeer peer) {
	    try {
	      peers_mon.enter();
	      peers.add(peer);
	    } finally {
	      peers_mon.exit();
	    }
	  }
	  
	  public void peerRemoved(PEPeer peer) {
	    try {
	      peers_mon.enter();
	      peers.remove(peer);
	    } finally {
	      peers_mon.exit();
	    }
	  }
  }
  
  private Map<DownloadManager,ManagerData>	dm_map = new IdentityHashMap<DownloadManager, ManagerData>();

  
  
  
  public PeersGraphicView() {
    angles = new double[NB_ANGLES];
    //deltaPerimeters = new double[NB_ANGLES];
    rs = new double[NB_ANGLES];
    deltaXXs = new double[NB_ANGLES];
    deltaXYs = new double[NB_ANGLES];
    deltaYXs = new double[NB_ANGLES];
    deltaYYs = new double[NB_ANGLES];
    
    for(int i = 0 ; i < NB_ANGLES ; i++) {
      angles[i] = 2 * i * Math.PI / NB_ANGLES - Math.PI;
      deltaXXs[i] = Math.cos(angles[i]);
      deltaXYs[i] = Math.sin(angles[i]);
      deltaYXs[i] = Math.cos(angles[i]+Math.PI / 2);
      deltaYYs[i] = Math.sin(angles[i]+Math.PI / 2);
    }
    
    this.peerComparator = new PeerComparator();
  } 
  
  public boolean
  isCloneable()
  {
	  return( true );
  }

  public UISWTViewCoreEventListener
  getClone()
  {
	  return( new PeersGraphicView());
  }
	
  private boolean comp_focused;
  private Object focus_pending_ds;
  
  private void
  setFocused( boolean foc )
  {
	  if ( foc ){
		  
		  comp_focused = true;
		  
		  dataSourceChanged( focus_pending_ds );
		  
	  }else{
		  
		  synchronized( dm_map ){
			  
			  focus_pending_ds = dm_map.values().toArray( new DownloadManager[ dm_map.size()]);
		  }
		  
		  dataSourceChanged( null );
		  
		  comp_focused = false;
	  }
  }
  
  private void dataSourceChanged(Object newDataSource) {
	  if ( !comp_focused ){
		  focus_pending_ds = newDataSource;
		  return;
	  }

	  // defer this util here so that a blocking call to get the IP doesn't hang UI construction

	  if ( my_flag == null ){

		  InetAddress ia = NetworkAdmin.getSingleton().getDefaultPublicAddress();

		  if ( ia != null ){

			  my_flag = ImageRepository.getCountryFlag( ia, false );
		  }
	  }

	  List<DownloadManager> newManagers = ViewUtils.getDownloadManagersFromDataSource( newDataSource );

	  synchronized( dm_map ){
		  
		  List<DownloadManager>	oldManagers = new ArrayList<DownloadManager>( dm_map.keySet());
		  
		  boolean	changed = false;
		  
		  for ( DownloadManager old: oldManagers ){
			  
			  if ( !newManagers.contains( old )){
				  
				  dm_map.remove(old).delete();
				  
				  changed = true;
			  }
		  }
		  for ( DownloadManager nu: newManagers ){
			  
			  if ( !oldManagers.contains( nu )){
			
				  dm_map.put( nu, new ManagerData( nu ));
				  
				  changed = true;
			  }
		  }

		  if ( !changed ){
			  
			  return;
		  }
		  
		  Utils.execSWTThread(new AERunnable() {
			  public void runSupport() {
				  if ( !dm_map.isEmpty()){
					  Utils.disposeComposite(panel, false);
				  } else {
					  ViewUtils.setViewRequiresOneDownload(panel);
				  }
			  }
		  });
	  }
  }

  private void 
  delete() 
  {
	synchronized( dm_map ){
			
		for ( Map.Entry<DownloadManager, ManagerData> dm_entry: dm_map.entrySet()){
				
			ManagerData		data		= dm_entry.getValue();

			data.delete();
		}
		
		dm_map.clear();
	}
  }

  private Composite getComposite() {    
    return panel;
  }
  
  private String getData() {
    return "PeersGraphicView.title.full";
  }

  private void initialize(Composite composite) {
    display = composite.getDisplay();
    
    panel = new Canvas(composite,SWT.NO_BACKGROUND);
            
    panel.addListener(SWT.MouseHover, new Listener() {
		public void handleEvent(Event event) {
			
			int	x = event.x;
			int y = event.y;
			
			String tt = "";

			synchronized( dm_map ){
				
				for ( Map.Entry<DownloadManager, ManagerData> dm_entry: dm_map.entrySet()){
				
					DownloadManager manager 	= dm_entry.getKey();
					ManagerData		data		= dm_entry.getValue();
		
					if ( 	x >= data.me_hit_x && x <= data.me_hit_x+OWN_SIZE &&
							y >= data.me_hit_y && y <= data.me_hit_y+OWN_SIZE ){
						
						if ( dm_map.size() > 1 ){
							
							tt = manager.getDisplayName() + "\r\n";
						}
						
						tt += DisplayFormatters.formatDownloadStatus( manager ) + ", " + 
								DisplayFormatters.formatPercentFromThousands(manager.getStats().getCompleted());
						
						break;
						
					}else{
						
						PEPeer target = null;
									
						for ( Map.Entry<PEPeer,int[]> entry: data.peer_hit_map.entrySet()){
							
							int[] loc = entry.getValue();
							
							int	loc_x = loc[0];
							int loc_y = loc[1];
							
							if ( 	x >= loc_x && x <= loc_x+PEER_SIZE &&
									y >= loc_y && y <= loc_y+PEER_SIZE ){
								
								target = entry.getKey();
								
								break;
							}
						}	
						
						if ( target != null ){
							
							PEPeerStats stats = target.getStats();
							
							String[] details = PeerUtils.getCountryDetails( target );
							
							String dstr = (details==null||details.length<2)?"":(" - " + details[0] + "/" + details[1]);
							/*
							if ( dm_map.size() > 1 ){
								
								tt = manager.getDisplayName() + "\r\n";
							}
							*/
							
							tt = target.getIp() + dstr + ", " + 
									DisplayFormatters.formatPercentFromThousands(target.getPercentDoneInThousandNotation()) + "\r\n" +
									"Up=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( stats.getDataSendRate() + stats.getProtocolSendRate()) + ", " +
									"Down=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( stats.getDataReceiveRate() + stats.getProtocolReceiveRate());
							
							break;
						}
					}
				}
			}
			
			panel.setToolTipText( tt );
		}
    });
    
    panel.addMouseListener(
    	new MouseAdapter()
    	{
    		@Override
    		public void 
    		mouseUp(
    			MouseEvent event) 
    		{
    			if ( event.button == 3 ){
    				
    	   			int	x = event.x;
        			int y = event.y;
        							
        			PEPeer 			target 			= null;
        			DownloadManager	target_manager 	= null;
        			
        			synchronized( dm_map ){
        				
	        			for ( Map.Entry<DownloadManager, ManagerData> dm_entry: dm_map.entrySet()){
	        				
	        				DownloadManager manager 	= dm_entry.getKey();
	        				ManagerData		data		= dm_entry.getValue();
	
		    				for( Map.Entry<PEPeer,int[]> entry: data.peer_hit_map.entrySet()){
		    					
		    					int[] loc = entry.getValue();
		    					
		    					int	loc_x = loc[0];
		    					int loc_y = loc[1];
		    					
		    					if ( 	x >= loc_x && x <= loc_x+PEER_SIZE &&
		    							y >= loc_y && y <= loc_y+PEER_SIZE ){
		    						
		    						target = entry.getKey();
		    						
		    						target_manager	= manager;
		    						
		    						break;
		    					}
		    				}
		    				
		    				if ( target != null ){
		    					
		    					break;
		    				}
	        			}
        			}
        			
    				if ( target == null ){
    					
    					return;
    				}
    				
    				Menu menu = panel.getMenu();
    				
    				if ( menu != null && !menu.isDisposed()){
    					
    					menu.dispose();
    				}
    				
    				menu = new Menu( panel );
    				
    				PeersView.fillMenu( menu, target, target_manager );
								
					final Point cursorLocation = Display.getCurrent().getCursorLocation();
					
					menu.setLocation( cursorLocation.x, cursorLocation.y );

    				menu.setVisible( true );
    			}
    		}
    		
    		public void 
    		mouseDoubleClick(
    			MouseEvent event )
    		{
    			int	x = event.x;
    			int y = event.y;
    				
    			synchronized( dm_map ){
    				
	       			for ( Map.Entry<DownloadManager, ManagerData> dm_entry: dm_map.entrySet()){
	    				
	    				DownloadManager manager 	= dm_entry.getKey();
	    				ManagerData		data		= dm_entry.getValue();
	
						for( Map.Entry<PEPeer,int[]> entry: data.peer_hit_map.entrySet()){
							
							int[] loc = entry.getValue();
							
							int	loc_x = loc[0];
							int loc_y = loc[1];
							
							if ( 	x >= loc_x && x <= loc_x+PEER_SIZE &&
									y >= loc_y && y <= loc_y+PEER_SIZE ){
								
								PEPeer target = entry.getKey();
								
									// ugly code to locate any associated 'PeersView' that we can locate the peer in
								
								try{
									String dm_id = "DMDetails_" + Base32.encode( manager.getTorrent().getHash());
									
									MdiEntry mdi_entry = UIFunctionsManager.getUIFunctions().getMDI().getEntry( dm_id );
								
									if ( mdi_entry != null ){
										
										mdi_entry.setDatasource(new Object[] { manager, target } );
									}
										
									Composite comp = panel.getParent();
									
									while( comp != null ){
										
										if ( comp instanceof CTabFolder ){
											
											CTabFolder tf = (CTabFolder)comp;
											
											CTabItem[] items = tf.getItems();
											
											for ( CTabItem item: items ){
												
												UISWTViewCore view = (UISWTViewCore)item.getData("TabbedEntry");
												
												UISWTViewEventListener listener = view.getEventListener();
												
												if ( listener instanceof UISWTViewEventListenerHolder ){
													
													listener = ((UISWTViewEventListenerHolder)listener).getDelegatedEventListener( view );
												}
												
												if ( listener instanceof PeersView ){
													
													tf.setSelection( item );
													
													Event ev = new Event();
													
													ev.item = item;
													
														// manual setSelection doesn't file selection event - derp
													
													tf.notifyListeners( SWT.Selection, ev );
													
													((PeersView)listener).selectPeer( target );
													
													return;
												}
											}
										}
											
										comp = comp.getParent();
									}
								}catch( Throwable e ){
									
								}
								
								break;
							}
						}
	       			}
				}		
    		}
    	});
    
    	// without this we get a transient blank when mousing in and out of the tab folder on OSX :(
    
    panel.addPaintListener(
    	new PaintListener(){
			public void paintControl(PaintEvent e) {
				doRefresh();
			}
		});
  }

  private void refresh() {
    doRefresh();
  }
  
  private void doRefresh() {
    //Comment the following line to enable the view
    //if(true) return;
    
	synchronized( dm_map ){
		
	    if (panel == null || panel.isDisposed()){
	    	return;
	    }
	    
	    if ( dm_map.size() == 0  ){
	    	GC gcPanel = new GC(panel);
	    	gcPanel.fillRectangle( panel.getBounds());
	    	gcPanel.dispose();
	    	return;
	    }
    	
	    int	num_dms = dm_map.size();
	    
	    Point panelSize = panel.getSize();

	    int	pw = panelSize.x;
	    int	ph = panelSize.y;
	    
	    int	h_cells;
	    int v_cells;
	    
	    if ( ph <= pw ){
	    	
	    	v_cells = 1;
	    	h_cells = pw/ph;
	    			
	    	double f = Math.sqrt(((double)num_dms)/(v_cells*h_cells));
	    	
	    	int factor = (int)Math.ceil(f);
	    	
	    	h_cells *= factor;
	    	v_cells = factor;
	    	
	    }else{
	    	
	    	v_cells = ph/pw;
	    	h_cells = 1;
	    	
	    			
	    	double f = Math.sqrt(((double)num_dms)/(v_cells*h_cells));
	    	
	    	int factor = (int)Math.ceil(f);
	    	
	    	v_cells *= factor;
	    	h_cells = factor;
	    }
	    
	    ph = h_cells==1?(ph/num_dms):(ph/v_cells);
	    pw = v_cells==1?(pw/num_dms):(pw/h_cells);
	    
	    //System.out.println( h_cells + "*" + v_cells + ": " + pw + "*" + ph );
	    
	    Point mySize 	= new Point( pw, ph );

	    int	num = 0;
	    
	    Point lastOffset = null;
	    
		for ( Map.Entry<DownloadManager, ManagerData> dm_entry: dm_map.entrySet()){
				
			DownloadManager manager 	= dm_entry.getKey();
			ManagerData		data		= dm_entry.getValue();

		    PEPeer[] sortedPeers;
		    try {      
		      data.peers_mon.enter();      
		      List<PEPeerTransport> connectedPeers = new ArrayList<PEPeerTransport>();
		      for (PEPeer peer : data.peers) {
		      	if (peer instanceof PEPeerTransport) {
		      		PEPeerTransport peerTransport = (PEPeerTransport) peer;
		      		if(peerTransport.getConnectionState() == PEPeerTransport.CONNECTION_FULLY_ESTABLISHED)
		      			connectedPeers.add(peerTransport);
		      	}
		      }
		      
		      sortedPeers = connectedPeers.toArray(new PEPeer[connectedPeers.size()]);      
		    } finally {
		    	data.peers_mon.exit();
		    }
		    
		    if(sortedPeers == null) return;
		    
		    for (int i=0;i<3;i++){
		    	try{
		    		
		    		Arrays.sort(sortedPeers,peerComparator);
		    		
		    		break;
		    		
		    	}catch( IllegalArgumentException e ){
		    		
		    		// can happen as peer data can change during sort and result in 'comparison method violates its general contract' error
		    	}
		    }
		    
		    int h = num%h_cells;
		    int v = num/h_cells;

		    Point myOffset	= new Point(h*pw,v*ph);
		    
		    render( manager, data, sortedPeers, mySize, myOffset);
		    
		    num++;
		    
		    lastOffset = myOffset;
		}
		
		int	rem_x = panelSize.x - (lastOffset.x + mySize.x );
		
		if ( rem_x > 0 ){
		  	GC gcPanel = new GC(panel);
	    	gcPanel.setBackground(Colors.white); 
	    	gcPanel.fillRectangle(lastOffset.x + mySize.x,lastOffset.y,rem_x,mySize.y);
	    	gcPanel.dispose();
		}
		
		int	rem_y = panelSize.y - (lastOffset.y + mySize.y );
		
		if ( rem_y > 0 ){
		  	GC gcPanel = new GC(panel);
	    	gcPanel.setBackground(Colors.white); 
	    	gcPanel.fillRectangle(0, lastOffset.y + mySize.y, panelSize.x, rem_y);
	    	gcPanel.dispose();
		}
	}
  }
  
  private void 
  render(
	DownloadManager		manager,
	ManagerData			data, 
	PEPeer[] 			sortedPeers,
	Point				panelSize,
	Point				panelOffset )
  {
	data.peer_hit_map.clear();
	      
    int	min_dim = Math.min( panelSize.x, panelSize.y );
    
    if ( min_dim <= 100 ){
    	OWN_SIZE = OWN_SIZE_MIN;
    }else if ( min_dim >= 400 ){
    	OWN_SIZE = OWN_SIZE_DEFAULT;
    }else{
    	int s_diff = OWN_SIZE_MAX - OWN_SIZE_MIN;
    	float rat = (min_dim - 100.0f)/(400-100);
    	
    	OWN_SIZE = OWN_SIZE_MIN + (int)(s_diff * rat );
    }
    
    
    int x0 = panelSize.x / 2;
    int y0 = panelSize.y / 2;  
    int a = x0 - 20;
    int b = y0 - 20;
    if(a < 10 || b < 10){
    	GC gcPanel = new GC(panel);
    	gcPanel.setBackground(Colors.white); 
    	gcPanel.fillRectangle(panelOffset.x,panelOffset.y,panelSize.x,panelSize.y);
    	gcPanel.dispose();
    	return;
    }
    
    if(data.oldSize == null || !data.oldSize.equals(panelSize)) {     
    	data.oldSize = panelSize;      
      perimeter = 0;
      for(int i = 0 ; i < NB_ANGLES ; i++) {
        rs[i] = Math.sqrt(1/(deltaYXs[i] * deltaYXs[i] / (a*a) + deltaYYs[i] * deltaYYs[i] / (b * b)));
        perimeter += rs[i];
      }
    }
    Image buffer = new Image(display,panelSize.x,panelSize.y);
    GC gcBuffer = new GC(buffer);    
    gcBuffer.setBackground(Colors.white);   
    gcBuffer.setForeground(Colors.blue);
    gcBuffer.fillRectangle(0,0,panelSize.x,panelSize.y);

    try {
      gcBuffer.setTextAntialias(SWT.ON);
      gcBuffer.setAntialias(SWT.ON);
    } catch(Exception e) {
    }
    
    gcBuffer.setBackground(Colors.blues[Colors.BLUES_MIDLIGHT]);      
    
    int nbPeers = sortedPeers.length;
    
    int iAngle = 0;
    double currentPerimeter = 0;    
    //double angle;
    double r;   

    for(int i = 0 ; i < nbPeers ; i++) {
      PEPeer peer = sortedPeers[i];
      do {
        //angle = angles[iAngle];
        r     = rs[iAngle];
        currentPerimeter += r;
        if(iAngle + 1 < NB_ANGLES) iAngle++;
      } while( currentPerimeter < i * perimeter / nbPeers);
            
      //angle = (4 * i - nbPeers) * Math.PI  / (2 * nbPeers) - Math.PI / 2;
      
      int[] triangle = new int[6];
      
      
      int percent_received 	= peer.getPercentDoneOfCurrentIncomingRequest();
      int percent_sent 		= peer.getPercentDoneOfCurrentOutgoingRequest();

      	// set up base line state
      

      boolean	drawLine = false;
      
      	// unchoked
      
      if ( !peer.isChokingMe() || percent_received >= 0 ){
      	gcBuffer.setForeground(Colors.blues[1] );
     	drawLine = true;
      }

      	// unchoking
      
      if ( !peer.isChokedByMe() || percent_sent >= 0 ){
  		gcBuffer.setForeground(Colors.blues[3]);
  		drawLine = true;
      }
         
      	// receiving from choked peer (fast request in)
      
      if ( !peer.isChokingMe() && peer.isUnchokeOverride() && peer.isInteresting()){
  		gcBuffer.setForeground(Colors.green);
  		drawLine = true;
      }
      
      	// sending to choked peer (fast request out)
      
      if ( peer.isChokedByMe() && percent_sent >= 0 ){
    	gcBuffer.setForeground(Colors.green);
    	drawLine = true;
      }
      
      if ( drawLine ){
		int x1 = x0 + (int) ( r * deltaYXs[iAngle] );
		int y1 = y0 + (int) ( r * deltaYYs[iAngle] );        
		gcBuffer.drawLine(x0,y0,x1,y1);
      }    
      
      
      
      if(percent_received >= 0) {
        gcBuffer.setBackground(Colors.blues[Colors.BLUES_MIDDARK]);
        double r1 = r - r * percent_received / 100;
        triangle[0] = (int) (x0 + (r1-10) * deltaYXs[iAngle] + 0.5);
        triangle[1] = (int) (y0 + (r1-10) * deltaYYs[iAngle] + 0.5);
        
        triangle[2] =  (int) (x0 + deltaXXs[iAngle] * 4 + (r1) * deltaYXs[iAngle] + 0.5);
        triangle[3] =  (int) (y0 + deltaXYs[iAngle] * 4 + (r1) * deltaYYs[iAngle] + 0.5);
        
        
        triangle[4] =  (int) (x0 - deltaXXs[iAngle] * 4 + (r1) * deltaYXs[iAngle] + 0.5);
        triangle[5] =  (int) (y0 - deltaXYs[iAngle] * 4 + (r1) * deltaYYs[iAngle] + 0.5);
        
        gcBuffer.fillPolygon(triangle); 
      }
      
      
      
      if(percent_sent >= 0) {
        gcBuffer.setBackground(Colors.blues[Colors.BLUES_MIDLIGHT]);
        double r1 = r * percent_sent / 100;
        triangle[0] = (int) (x0 + r1 * deltaYXs[iAngle] + 0.5);
        triangle[1] = (int) (y0 + r1 * deltaYYs[iAngle] + 0.5);
        
        triangle[2] =  (int) (x0 + deltaXXs[iAngle] * 4 + (r1-10) * deltaYXs[iAngle] + 0.5);
        triangle[3] =  (int) (y0 + deltaXYs[iAngle] * 4 + (r1-10) * deltaYYs[iAngle] + 0.5);
        
        
        triangle[4] =  (int) (x0 - deltaXXs[iAngle] * 4 + (r1-10) * deltaYXs[iAngle] + 0.5);
        triangle[5] =  (int) (y0 - deltaXYs[iAngle] * 4 + (r1-10) * deltaYYs[iAngle] + 0.5);
        gcBuffer.fillPolygon(triangle); 
      }
      
      
      
      int x1 = x0 + (int) (r * deltaYXs[iAngle]);
      int y1 = y0 + (int) (r * deltaYYs[iAngle]);
      gcBuffer.setBackground(Colors.blues[Colors.BLUES_MIDDARK]);
      if(peer.isSnubbed()) {
        gcBuffer.setBackground(Colors.grey);
      }
      
      /*int PS = (int) (PEER_SIZE);      
        if (deltaXY == 0) {
          PS = (int) (PEER_SIZE * 2);
        } else {
          if (deltaYY > 0) {
            PS = (int) (PEER_SIZE / deltaXY);
          }
        }*/
      //PieUtils.drawPie(gcBuffer,(x1 - PS / 2),y1 - PS / 2,PS,PS,peer.getPercentDoneInThousandNotation() / 10);
      
      int peer_x = x1 - PEER_SIZE / 2;
      int peer_y = y1 - PEER_SIZE / 2;
      
      data.peer_hit_map.put( peer, new int[]{ peer_x + panelOffset.x, peer_y + panelOffset.y });
      
      Image flag = ImageRepository.getCountryFlag( peer, false );
      if ( flag != null ){
    	  PieUtils.drawPie(gcBuffer, flag, peer_x, peer_y,PEER_SIZE,PEER_SIZE,peer.getPercentDoneInThousandNotation() / 10, true );
      }else{
      
    	  PieUtils.drawPie(gcBuffer, peer_x, peer_y,PEER_SIZE,PEER_SIZE,peer.getPercentDoneInThousandNotation() / 10);
      }
      //gcBuffer.drawText(peer.getIp() , x1 + 8 , y1 , true);
    }
    
    gcBuffer.setBackground(Colors.blues[Colors.BLUES_MIDDARK]);
    
    data.me_hit_x = x0 - OWN_SIZE / 2;
    data.me_hit_y = y0 - OWN_SIZE / 2;
      
   	PieUtils.drawPie(gcBuffer, data.me_hit_x, data.me_hit_y,OWN_SIZE,OWN_SIZE,manager.getStats().getCompleted() / 10);

    if ( my_flag != null ){
    	PieUtils.drawPie(gcBuffer, my_flag, data.me_hit_x, data.me_hit_y,OWN_SIZE,OWN_SIZE,manager.getStats().getCompleted() / 10, false );
    }
    
    data.me_hit_x += panelOffset.x;
    data.me_hit_y += panelOffset.y;
    
    gcBuffer.dispose();
    GC gcPanel = new GC(panel);
    gcPanel.drawImage(buffer,panelOffset.x,panelOffset.y);
    gcPanel.dispose();
    buffer.dispose();   
  }
  

	public boolean eventOccurred(UISWTViewEvent event) {
    switch (event.getType()) {
      case UISWTViewEvent.TYPE_CREATE:
      	swtView = event.getView();
      	swtView.setTitle(MessageText.getString(getData()));
      	swtView.setToolBarListener(this);
        break;

      case UISWTViewEvent.TYPE_DESTROY:
        delete();
        break;

      case UISWTViewEvent.TYPE_INITIALIZE:
        initialize((Composite)event.getData());
        break;

      case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
      	Messages.updateLanguageForControl(getComposite());
      	swtView.setTitle(MessageText.getString(getData()));
        break;

      case UISWTViewEvent.TYPE_DATASOURCE_CHANGED:
      	dataSourceChanged(event.getData());
        break;
        
      case UISWTViewEvent.TYPE_FOCUSGAINED:
        	String id = "DMDetails_Swarm";
        	
        	setFocused( true );	// do this before next code as it can pick up the corrent 'manager' ref
			synchronized( dm_map ){
				
				if ( dm_map.isEmpty()){
					
					SelectedContentManager.changeCurrentlySelectedContent(id, null);
					
				}else{
					
					DownloadManager manager 	= dm_map.keySet().iterator().next();
					
	        		if (manager.getTorrent() != null) {
	        			id += "." + manager.getInternalName();
	        		} else {
	        			id += ":" + manager.getSize();
	        		}
					
	        		SelectedContentManager.changeCurrentlySelectedContent(id,
	        				new SelectedContent[] {
	        				new SelectedContent(manager)
	        		});
				}
			}
			break;
      case UISWTViewEvent.TYPE_FOCUSLOST:
    	  setFocused( false );
    	  SelectedContentManager.clearCurrentlySelectedContent();
    	  break;
      case UISWTViewEvent.TYPE_REFRESH:
        refresh();
        break;
    }

    return true;
  }
	
	/* (non-Javadoc)
	 * @see org.gudy.azureus2.plugins.ui.toolbar.UIToolBarActivationListener#toolBarItemActivated(com.aelitis.azureus.ui.common.ToolBarItem, long, java.lang.Object)
	 */
	public boolean toolBarItemActivated(ToolBarItem item, long activationType,
			Object datasource) {
		return false; // default handler will handle it
	}

	/* (non-Javadoc)
	 * @see org.gudy.azureus2.plugins.ui.UIPluginViewToolBarListener#refreshToolBarItems(java.util.Map)
	 */
	public void refreshToolBarItems(Map<String, Long> list) {
		Map<String, Long> states = TorrentUtil.calculateToolbarStates(
				SelectedContentManager.getCurrentlySelectedContent(), null);
		list.putAll(states);
	}
}
