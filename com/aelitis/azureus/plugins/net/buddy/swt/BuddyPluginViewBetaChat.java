/*
 * Created on Apr 26, 2008
 * Created by Paul Gardner
 * 
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package com.aelitis.azureus.plugins.net.buddy.swt;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.AENetworkClassifier;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.AddressUtils;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.FrequencyLimitedDispatcher;
import org.gudy.azureus2.core3.util.RandomUtils;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.UrlUtils;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.sharing.ShareManager;
import org.gudy.azureus2.plugins.sharing.ShareResourceDir;
import org.gudy.azureus2.plugins.sharing.ShareResourceFile;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.UIManagerEvent;
import org.gudy.azureus2.plugins.utils.LocaleUtilities;
import org.gudy.azureus2.plugins.utils.subscriptions.SubscriptionManager;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import org.gudy.azureus2.pluginsimpl.local.utils.FormattersImpl;
import org.gudy.azureus2.ui.swt.Messages;
import org.gudy.azureus2.ui.swt.URLTransfer;
import org.gudy.azureus2.ui.swt.Utils;
import org.gudy.azureus2.ui.swt.components.BufferedLabel;
import org.gudy.azureus2.ui.swt.components.LinkLabel;
import org.gudy.azureus2.ui.swt.components.shell.ShellFactory;
import org.gudy.azureus2.ui.swt.mainwindow.ClipboardCopy;
import org.gudy.azureus2.ui.swt.mainwindow.Colors;
import org.gudy.azureus2.ui.swt.mainwindow.TorrentOpener;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;

import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.util.AZ3Functions;
import com.aelitis.azureus.plugins.net.buddy.BuddyPlugin;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.*;
import com.aelitis.azureus.ui.swt.imageloader.ImageLoader;

public class 
BuddyPluginViewBetaChat 
	implements ChatListener
{
	private static final boolean TEST_LOOPBACK_CHAT = System.getProperty( "az.chat.loopback.enable", "0" ).equals( "1" );
	private static final boolean DEBUG_ENABLED		= BuddyPluginBeta.DEBUG_ENABLED;

	private static final int	MAX_MSG_LENGTH	= 400;
	
	private static final Set<BuddyPluginViewBetaChat>	active_windows = new HashSet<BuddyPluginViewBetaChat>();
	
	private static boolean auto_ftux_popout_done	= false;
	
	protected static void
	createChatWindow(
		BuddyPluginView	view,
		BuddyPlugin		plugin,
		ChatInstance	chat )	
	{
		createChatWindow( view, plugin, chat, false );
	}
	
	protected static void
	createChatWindow(
		BuddyPluginView	view,
		BuddyPlugin		plugin,
		ChatInstance	chat,
		boolean			force_popout )
	{
		for ( BuddyPluginViewBetaChat win: active_windows ){
			
			if ( win.getChat() == chat ){
				
				Shell existing = win.getShell();
				
				if ( existing.isVisible()){
					
					existing.setActive();
				}
				
				chat.destroy();
				
				return;
			}
		}
		
		if ( !force_popout ){
			
			if ( plugin.getBeta().getWindowsToSidebar()){
				
				final AZ3Functions.provider az3 = AZ3Functions.getProvider();
				
				if ( az3 != null ){
					
					if ( az3.openChat( chat.getNetwork(), chat.getKey())){
						
						chat.destroy();
						
						return;
					}
					
				}
			}
		}
		
		new BuddyPluginViewBetaChat( view, plugin, chat );
	}
	
	private final BuddyPluginView		view;
	private final BuddyPlugin			plugin;
	private final BuddyPluginBeta		beta;
	private final ChatInstance			chat;
	
	private final LocaleUtilities		lu;
	
	private Shell 					shell;
	
	private StyledText 				log;
	private StyleRange[]			log_styles = new StyleRange[0];
	
	private BufferedLabel			table_header;
	private Table					buddy_table;
	private BufferedLabel		 	status;
	
	private Button 					shared_nick_button;
	private Text 					nickname;
	
	private Text 					input_area;
	private Button					rss_button;
	
	private DropTarget[]			drop_targets;
	
	private LinkedHashMap<ChatMessage,Integer>	messages		= new LinkedHashMap<ChatMessage,Integer>();
	private List<ChatParticipant>				participants 	= new ArrayList<ChatParticipant>();
	
	private Map<ChatParticipant,ChatMessage>	participant_last_message_map = new HashMap<ChatParticipant, ChatMessage>();
	
	private boolean		table_resort_required;
	
	private Font	italic_font;
	private Font	bold_font;
	private Font	big_font;
	private Font	small_font;
	
	private Color	ftux_dark_bg;
	private Color	ftux_dark_fg;
	private Color	ftux_light_bg;
	
	private boolean	ftux_ok;
	private boolean	build_complete;
	
	private
	BuddyPluginViewBetaChat(
		BuddyPluginView	_view,
		BuddyPlugin		_plugin,
		ChatInstance	_chat )
	{
		view	= _view;
		plugin	= _plugin;
		chat	= _chat;
		beta	= plugin.getBeta();
		
		lu		= plugin.getPluginInterface().getUtilities().getLocaleUtilities();
		
		if ( beta.getStandAloneWindows()){
			
			shell = ShellFactory.createShell( (Shell)null, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
			
		}else{
			
			shell = ShellFactory.createMainShell( SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
		}
		
		shell.addListener(
			SWT.Show,
			new Listener() {
				public void handleEvent(Event event) {
					activate();
				}
			});
		
		shell.setText( lu.getLocalisedMessageText( "label.chat" ) + ": " + chat.getName());
				
		Utils.setShellIcon(shell);
		
		build( shell );
		
		shell.addListener(
			SWT.Traverse, 
			new Listener() 
			{	
				public void 
				handleEvent(
					Event e ) 
				{
					if ( e.character == SWT.ESC){
					
						close();
				}
			}
			});
		
		shell.addControlListener(
			new ControlListener()
			{
				private volatile Rectangle last_position;
				
				private FrequencyLimitedDispatcher disp = 
					new FrequencyLimitedDispatcher(
						new AERunnable() {
							
							@Override
							public void 
							runSupport() 
							{
								Rectangle	pos = last_position;
								
								String str = pos.x+","+pos.y+","+pos.width+","+pos.height;
																
								COConfigurationManager.setParameter( "azbuddy.dchat.ui.last.win.pos", str );
							}
						},
					1000 );
				
				public void 
				controlResized(
					ControlEvent e) 
				{
					handleChange();
				}
				
				public void 
				controlMoved(
					ControlEvent e) 
				{
					handleChange();
				}
				
				private void
				handleChange()
				{
					last_position = shell.getBounds();
					
					disp.dispatch();
				}
			});
	
		
		int DEFAULT_WIDTH	= 500;
		int DEFAULT_HEIGHT	= 500;
		int MIN_WIDTH		= 300;
		int MIN_HEIGHT		= 150;

		
		String str_pos = COConfigurationManager.getStringParameter( "azbuddy.dchat.ui.last.win.pos", "" );

		Rectangle last_bounds = null;

		try{
			if ( str_pos != null && str_pos.length() > 0 ){
				
				String[] bits = str_pos.split( "," );
				
				if ( bits.length == 4 ){
					
					int[]	 i_bits = new int[4];
					
					for ( int i=0;i<bits.length;i++){
						
						i_bits[i] = Integer.parseInt( bits[i] );
					}
														 
					last_bounds = 
						new Rectangle(
							i_bits[0], 
							i_bits[1], 
							Math.max( MIN_WIDTH, i_bits[2] ),
							Math.max( MIN_HEIGHT, i_bits[3] ));
				}
			}
		}catch( Throwable e ){
		}
				
	    //Utils.createURLDropTarget(shell, input_area);
	    
		if ( active_windows.size() > 0 ){
			
			int	max_x = 0;
			int max_y = 0;
			
			for ( BuddyPluginViewBetaChat window: active_windows ){
				
				if ( !window.shell.isDisposed()){
					
					Rectangle rect = window.shell.getBounds();
					
					max_x = Math.max( max_x, rect.x );
					max_y = Math.max( max_y, rect.y );
				}
			}
				
			Rectangle rect = new Rectangle( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
							
			rect.x = max_x + 16;
			rect.y = max_y + 16;
			
			if ( last_bounds != null ){
				
				rect.width 	= last_bounds.width;
				rect.height	=  last_bounds.height;
			}
			
			shell.setBounds( rect );
									
			Utils.verifyShellRect( shell, true );
			
		}else{
  			
			if ( last_bounds != null ){
										
				shell.setBounds( last_bounds );
					    
				Utils.verifyShellRect( shell, true );
						 
			}else{
			
			    shell.setSize( DEFAULT_WIDTH, DEFAULT_HEIGHT );

				Utils.centreWindow(shell);
			}
		}
		
	    active_windows.add( this );

	    shell.addDisposeListener(
	    	new DisposeListener(){
				public void 
				widgetDisposed(DisposeEvent e)
				{
					active_windows.remove( BuddyPluginViewBetaChat.this );
				}
			});
	    
	    shell.open();
	    
	    shell.forceActive();
	}
	
	protected
	BuddyPluginViewBetaChat(
		BuddyPluginView	_view,
		BuddyPlugin		_plugin,
		ChatInstance	_chat,
		Composite		_parent )
	{
		view	= _view;
		plugin	= _plugin;
		chat	= _chat;
		beta	= plugin.getBeta();

		lu		= plugin.getPluginInterface().getUtilities().getLocaleUtilities();
		
		build( _parent );
	}
	
	private Shell
	getShell()
	{
		return( shell );
	}
	
	private ChatInstance
	getChat()
	{
		return( chat );
	}
	
	private void
	build(
		Composite		parent )
	{
		view.registerUI( chat );
		
		boolean public_chat = !chat.isPrivateChat();
	
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		GridData grid_data = new GridData(GridData.FILL_BOTH );
		Utils.setLayoutData(parent, grid_data);

		Composite sash_area = new Composite( parent, SWT.NONE );
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		sash_area.setLayout(layout);
		
		grid_data = new GridData(GridData.FILL_BOTH );
		grid_data.horizontalSpan = 2;
		Utils.setLayoutData(sash_area, grid_data);
		
	    final SashForm sash = new SashForm(sash_area,SWT.HORIZONTAL );
	    grid_data = new GridData(GridData.FILL_BOTH );
	    Utils.setLayoutData(sash, grid_data);
	    	    
		final Composite lhs = new Composite(sash, SWT.NONE);
		
		lhs.addDisposeListener(
				new DisposeListener()
				{
					public void 
					widgetDisposed(
						DisposeEvent arg0 ) 
					{
						Font[] fonts = { italic_font, bold_font, big_font, small_font };
						
						for ( Font f: fonts ){
							
							if ( f != null ){
								
								f.dispose();
							}
						}
						
						Color[] colours = { ftux_dark_bg, ftux_dark_fg, ftux_light_bg };
						
						for ( Color c: colours ){
							
							if ( c != null ){
								
								c.dispose();
							}
						}
						
						if ( drop_targets != null ){
							
							for ( DropTarget dt: drop_targets ){
								
								dt.dispose();
							}
						}
						
						closed();
					}
				});		
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 4;
		layout.marginLeft = 4;
		lhs.setLayout(layout);
		grid_data = new GridData(GridData.FILL_BOTH );
		grid_data.widthHint = 300;
		Utils.setLayoutData(lhs, grid_data);
		
		final Label menu_drop = new Label( lhs, SWT.NULL );
		
		FontData fontData = menu_drop.getFont().getFontData()[0];
		
		Display display = menu_drop.getDisplay();
		
		italic_font = new Font( display, new FontData( fontData.getName(), fontData.getHeight(), SWT.ITALIC ));
		bold_font 	= new Font( display, new FontData( fontData.getName(), fontData.getHeight(), SWT.BOLD ));
		big_font 	= new Font( display, new FontData( fontData.getName(), (int)(fontData.getHeight()*1.5), SWT.BOLD ));
		small_font 	= new Font( display, new FontData( fontData.getName(), (int)(fontData.getHeight()*0.5), SWT.BOLD ));

		ftux_dark_bg 	= new Color( display, 183, 200, 212 );
		ftux_dark_fg 	= new Color( display, 0, 81, 134 );
		ftux_light_bg 	= new Color( display, 236, 242, 246 );
		
		status = new BufferedLabel( lhs, SWT.LEFT | SWT.DOUBLE_BUFFERED );
		grid_data = new GridData(GridData.FILL_HORIZONTAL);
		
		Utils.setLayoutData(status, grid_data);
		status.setText( MessageText.getString( "PeersView.state.pending" ));
		
		Image image = ImageLoader.getInstance().getImage( "menu_down" );
		menu_drop.setImage( image );
		grid_data = new GridData();
		grid_data.widthHint=image.getBounds().width;
		grid_data.heightHint=image.getBounds().height;
		Utils.setLayoutData(menu_drop, grid_data);

		menu_drop.setCursor(menu_drop.getDisplay().getSystemCursor(SWT.CURSOR_HAND));		

		Control status_control = status.getControl();
		
		final Menu status_menu = new Menu( status_control );
		
		status.getControl().setMenu( status_menu );
		menu_drop.setMenu( status_menu );
		
		menu_drop.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				try{
					Point p = status_menu.getDisplay().map( menu_drop, null, event.x, event.y );
					
					status_menu.setLocation( p );
					
					status_menu.setVisible(true);
					
				}catch( Throwable e ){
					
					Debug.out( e);
				}
			}
		});

		if ( public_chat ){
				
			Menu status_clip_menu = new Menu(lhs.getShell(), SWT.DROP_DOWN);
			MenuItem status_clip_item = new MenuItem( status_menu, SWT.CASCADE);
			status_clip_item.setMenu(status_clip_menu);
			status_clip_item.setText(  MessageText.getString( "label.copy.to.clipboard" ));
			
			MenuItem status_mi = new MenuItem( status_clip_menu, SWT.PUSH );
			status_mi.setText( MessageText.getString( "azbuddy.dchat.copy.channel.key" ));
			
			status_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							ClipboardCopy.copyToClipBoard( chat.getKey());
						}
					});
			
			status_mi = new MenuItem( status_clip_menu, SWT.PUSH );
			status_mi.setText( MessageText.getString( "azbuddy.dchat.copy.channel.url" ));
			
			status_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							ClipboardCopy.copyToClipBoard( chat.getURL());
						}
					});
			
			status_mi = new MenuItem( status_clip_menu, SWT.PUSH );
			status_mi.setText( MessageText.getString( "azbuddy.dchat.copy.rss.url" ));
			
			status_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							ClipboardCopy.copyToClipBoard( "azplug:?id=azbuddy&arg=" + UrlUtils.encode( chat.getURL() + "&format=rss" ));
						}
					});
			
			status_mi = new MenuItem( status_clip_menu, SWT.PUSH );
			status_mi.setText( MessageText.getString( "azbuddy.dchat.copy.channel.pk" ));
			
			status_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							ClipboardCopy.copyToClipBoard( Base32.encode( chat.getPublicKey()));
						}
					});
							
			status_mi = new MenuItem( status_clip_menu, SWT.PUSH );
			status_mi.setText( MessageText.getString( "azbuddy.dchat.copy.channel.export" ));
			
			status_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							ClipboardCopy.copyToClipBoard( chat.export());
						}
					});
			
			if ( !chat.isManaged()){
				
				Menu status_channel_menu = new Menu(lhs.getShell(), SWT.DROP_DOWN);
				MenuItem status_channel_item = new MenuItem( status_menu, SWT.CASCADE);
				status_channel_item.setMenu(status_channel_menu);
				status_channel_item.setText(  MessageText.getString( "azbuddy.dchat.rchans" ));
		
					// Managed channel
				
				status_mi = new MenuItem( status_channel_menu, SWT.PUSH );
				status_mi.setText( MessageText.getString( "azbuddy.dchat.rchans.managed" ));
		
				status_mi.addSelectionListener(
						new SelectionAdapter() {				
							public void 
							widgetSelected(
								SelectionEvent event ) 
							{								
								try{
									ChatInstance inst = chat.getManagedChannel();
									
									BuddyPluginViewBetaChat.createChatWindow( view, plugin, inst );
									
								}catch( Throwable e ){
									
									Debug.out( e );
								}
							}
						});
				
					// RO channel
				
				status_mi = new MenuItem( status_channel_menu, SWT.PUSH );
				status_mi.setText( MessageText.getString( "azbuddy.dchat.rchans.ro" ));
		
				status_mi.addSelectionListener(
						new SelectionAdapter() {				
							public void 
							widgetSelected(
								SelectionEvent event ) 
							{
								try{
									ChatInstance inst = chat.getReadOnlyChannel();
									
									createChatWindow( view, plugin, inst );
									
								}catch( Throwable e ){
									
									Debug.out( e );
								}						
							}
						});
				
					// Random sub-channel
				
				status_mi = new MenuItem( status_channel_menu, SWT.PUSH );
				status_mi.setText( MessageText.getString( "azbuddy.dchat.rchans.rand" ));
		
				status_mi.addSelectionListener(
						new SelectionAdapter() {				
							public void 
							widgetSelected(
								SelectionEvent event ) 
							{
								try{
									byte[]	rand = new byte[20];
									
									RandomUtils.nextSecureBytes( rand );
									
									ChatInstance inst = beta.getChat( chat.getNetwork(), chat.getKey() + " {" + Base32.encode( rand ) + "}" );
									
									createChatWindow( view, plugin, inst );
									
								}catch( Throwable e ){
									
									Debug.out( e );
								}						
							}
						});
				if ( beta.isI2PAvailable()){
					
					status_mi = new MenuItem( status_channel_menu, SWT.PUSH );
					status_mi.setText( MessageText.getString(  chat.getNetwork()==AENetworkClassifier.AT_I2P?"azbuddy.dchat.rchans.pub":"azbuddy.dchat.rchans.anon" ));
			
					status_mi.addSelectionListener(
							new SelectionAdapter() {				
								public void 
								widgetSelected(
									SelectionEvent event ) 
								{
									try{
										ChatInstance inst = beta.getChat( chat.getNetwork()==AENetworkClassifier.AT_I2P?AENetworkClassifier.AT_PUBLIC:AENetworkClassifier.AT_I2P, chat.getKey());
										
										createChatWindow( view, plugin, inst );
										
									}catch( Throwable e ){
										
										Debug.out( e );
									}						
								}
							});
				}
			}
							
			final MenuItem fave_mi = new MenuItem( status_menu, SWT.CHECK );
			fave_mi.setText( MessageText.getString( "label.fave" ));
			
			fave_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							chat.setFavourite( fave_mi.getSelection());
						}
					});
			
			final AZ3Functions.provider az3 = AZ3Functions.getProvider();
			
			if ( az3 != null ){
				
				final MenuItem sis_mi = new MenuItem( status_menu, SWT.PUSH );
				sis_mi.setText( MessageText.getString( Utils.isAZ2UI()?"label.show.in.tab":"label.show.in.sidebar" ));
				
				sis_mi.addSelectionListener(
						new SelectionAdapter() {				
							public void 
							widgetSelected(
								SelectionEvent e ) 
							{
								az3.openChat( chat.getNetwork(), chat.getKey());
							}
						});
			}
			
			addFriendsMenu( status_menu );

				// advanced
			
			final Menu advanced_menu = new Menu(status_menu.getShell(), SWT.DROP_DOWN);
			MenuItem advanced_menu_item = new MenuItem( status_menu, SWT.CASCADE);
			advanced_menu_item.setMenu(advanced_menu);
			advanced_menu_item.setText(  MessageText.getString( "MyTorrentsView.menu.advancedmenu" ));

			final MenuItem persist_mi = new MenuItem( advanced_menu, SWT.CHECK );
			persist_mi.setText( MessageText.getString( "azbuddy.dchat.save.messages" ));
			
			persist_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							chat.setSaveMessages( persist_mi.getSelection());
						}
					});
			
			final MenuItem log_mi = new MenuItem( advanced_menu, SWT.CHECK );
			log_mi.setText( MessageText.getString( "azbuddy.dchat.log.messages" ));
			
			log_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							chat.setLogMessages( log_mi.getSelection());
						}
					});		
			
			final MenuItem automute_mi = new MenuItem( advanced_menu, SWT.CHECK );
			automute_mi.setText( MessageText.getString( "azbuddy.dchat.auto.mute" ));
			
			automute_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							chat.setAutoMute( automute_mi.getSelection());
						}
					});		
			
			final MenuItem postnotifications_mi = new MenuItem( advanced_menu, SWT.CHECK );
			postnotifications_mi.setText( MessageText.getString( "azbuddy.dchat.post.to.notifcations" ));
			
			postnotifications_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							chat.setEnableNotificationsPost( postnotifications_mi.getSelection());
						}
					});	
			
			final MenuItem disableindicators_mi = new MenuItem( advanced_menu, SWT.CHECK );
			disableindicators_mi.setText( MessageText.getString( "azbuddy.dchat.disable.msg.indicators" ));
			
			disableindicators_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							chat.setDisableNewMsgIndications( disableindicators_mi.getSelection());
						}
					});	
			
				// setup menu
			
			status_menu.addMenuListener(
					new MenuAdapter() 
					{
						public void 
						menuShown(
							MenuEvent e ) 
						{
							fave_mi.setSelection( chat.isFavourite());
							persist_mi.setSelection( chat.getSaveMessages());
							log_mi.setSelection( chat.getLogMessages());
							automute_mi.setSelection( chat.getAutoMute());
							postnotifications_mi.setSelection( chat.getEnableNotificationsPost());
							boolean disable_indications = chat.getDisableNewMsgIndications();
							disableindicators_mi.setSelection( disable_indications );
							postnotifications_mi.setEnabled( !disable_indications );
						}
					});
		}else{
			
			final Menu status_priv_menu = new Menu(lhs.getShell(), SWT.DROP_DOWN);
			MenuItem status_priv_item = new MenuItem( status_menu, SWT.CASCADE);
			status_priv_item.setMenu(status_priv_menu);
			status_priv_item.setText(  MessageText.getString( "label.private.chat" ));
					
			SelectionAdapter listener = 
				new SelectionAdapter()
				{				
					public void 
					widgetSelected(
						SelectionEvent e ) 
					{
						beta.setPrivateChatState((Integer)((MenuItem)e.widget).getData());
					}
				};
			
			MenuItem status_mi = new MenuItem( status_priv_menu, SWT.RADIO );
			status_mi.setText( MessageText.getString( "devices.contextmenu.od.enabled" ));
			status_mi.setData( BuddyPluginBeta.PRIVATE_CHAT_ENABLED );
			
			status_mi.addSelectionListener( listener );
		
			status_mi = new MenuItem( status_priv_menu, SWT.RADIO );
			status_mi.setText( MessageText.getString( "label.pinned.only" ));
			status_mi.setData( BuddyPluginBeta.PRIVATE_CHAT_PINNED_ONLY );
			
			status_mi.addSelectionListener( listener );

			status_mi = new MenuItem( status_priv_menu, SWT.RADIO );
			status_mi.setText( MessageText.getString( "pairing.status.disabled" ));
			status_mi.setData( BuddyPluginBeta.PRIVATE_CHAT_DISABLED );
			
			status_mi.addSelectionListener( listener );

			
			status_priv_menu.addMenuListener(
				new MenuAdapter() 
				{
					public void 
					menuShown(
						MenuEvent e ) 
					{
						int pc_state = beta.getPrivateChatState();
						
						for ( MenuItem mi: status_priv_menu.getItems()){
							
							mi.setSelection( pc_state == (Integer)mi.getData());
						}
					}
				});
			
			addFriendsMenu( status_menu );
			
			final MenuItem keep_alive_mi = new MenuItem( status_menu, SWT.CHECK );
			keep_alive_mi.setText( MessageText.getString( "label.keep.alive" ));
			
			status_menu.addMenuListener(
				new MenuAdapter() 
				{
					public void 
					menuShown(
						MenuEvent e ) 
					{
						keep_alive_mi.setSelection( chat.getUserData( "AC:KeepAlive" ) != null );
					}
				});
			
			keep_alive_mi.addSelectionListener(
					new SelectionAdapter() {				
						public void 
						widgetSelected(
							SelectionEvent e ) 
						{
							ChatInstance clone = (ChatInstance)chat.getUserData( "AC:KeepAlive" );
							
							if ( clone != null ){
								
								clone.destroy();
								
								clone = null;
								
							}else{
								
								try{
									clone = chat.getClone();
									
								}catch( Throwable f ){
									
								}
							}
							
							chat.setUserData( "AC:KeepAlive", clone );
						}
					});
			
			final AZ3Functions.provider az3 = AZ3Functions.getProvider();
			
			if ( az3 != null ){
				
				final MenuItem sis_mi = new MenuItem( status_menu, SWT.PUSH );
				sis_mi.setText( MessageText.getString( "label.show.in.sidebar" ));
				
				sis_mi.addSelectionListener(
						new SelectionAdapter() {				
							public void 
							widgetSelected(
								SelectionEvent e ) 
							{
								az3.openChat( chat.getNetwork(), chat.getKey());
							}
						});
			}
		}
		
		final Composite ftux_stack = new Composite(lhs, SWT.NONE);
		grid_data = new GridData(GridData.FILL_BOTH );
		grid_data.horizontalSpan = 2;
		Utils.setLayoutData(ftux_stack,  grid_data );
		
        final StackLayout stack_layout = new StackLayout();
        ftux_stack.setLayout(stack_layout);
        
		final Composite log_holder = new Composite(ftux_stack, SWT.BORDER);
		
		final Composite ftux_holder = new Composite(ftux_stack, SWT.BORDER);
		
			// FTUX panel
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		ftux_holder.setLayout(layout);
		
		ftux_holder.setBackground( ftux_light_bg );

			// top info
		
		Composite ftux_top_area = new Composite( ftux_holder, SWT.NULL );
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		ftux_top_area.setLayout(layout);
		
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 2;
		grid_data.heightHint = 30;
		Utils.setLayoutData(ftux_top_area, grid_data);
		ftux_top_area.setBackground( ftux_dark_bg );

		
		Label ftux_top = new Label( ftux_top_area, SWT.WRAP );
		grid_data = new GridData(SWT.LEFT, SWT.CENTER, true, true );
		grid_data.horizontalIndent = 8;
		Utils.setLayoutData(ftux_top, grid_data);
		
		ftux_top.setAlignment( SWT.LEFT );
		ftux_top.setBackground( ftux_dark_bg );
		ftux_top.setForeground( ftux_dark_fg );
		ftux_top.setFont( big_font );
		ftux_top.setText( MessageText.getString( "azbuddy.dchat.ftux.welcome" ));
		
			// middle info
		
		Label ftux_hack = new Label( ftux_holder, SWT.NULL );
		grid_data = new GridData();
		grid_data.heightHint=40;
		grid_data.widthHint=0;
		Utils.setLayoutData(ftux_hack, grid_data);
		
		final StyledText ftux_middle = new StyledText( ftux_holder, SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP | SWT.NO_FOCUS );
		
		grid_data = new GridData(GridData.FILL_BOTH );
		grid_data.horizontalSpan = 1;
		grid_data.verticalIndent = 4;
		grid_data.horizontalIndent = 16;
		Utils.setLayoutData(ftux_middle, grid_data);
		
		ftux_middle.setBackground( ftux_light_bg );
				
		String info1_text = 
		"Vuze chat allows you to communicate with other Vuze users directly by sending and receiving messages.\n" +
		"It is a decentralized chat system - there are no central servers involved, all messages are passed directly between Vuze users.\n" +
		"Consequently Vuze has absolutely no control over message content. In particular no mechanism exists (nor is possible) for Vuze to moderate or otherwise control either messages or the users that send messages.";
		
		String info2_text =
		"I UNDERSTAND AND AGREE that Vuze has no responsibility whatsoever with my enabling this function and using chat.";
		
		String[] info_lines = info1_text.split( "\n" );
		
		for ( String line: info_lines ){
		
			ftux_middle.append( line );
			
			if ( line != info_lines[info_lines.length-1] ){
			
				ftux_middle.append( "\n" );
				
				int	pos = ftux_middle.getText().length();
				
					// zero width space in large font to get smaller paragraph spacing 
				
				ftux_middle.append( "\u200B" );
				
				StyleRange styleRange = new StyleRange();
				styleRange.start = pos;
				styleRange.length = 1;
				styleRange.font = big_font;
				
				ftux_middle.setStyleRange( styleRange );
			}
		}

			// checkbox area
		
		Composite ftux_check_area = new Composite( ftux_holder, SWT.NULL );
		layout = new GridLayout();
		layout.marginLeft = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		ftux_check_area.setLayout(layout);
		
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 2;
		Utils.setLayoutData(ftux_check_area,  grid_data );
		ftux_check_area.setBackground(  ftux_light_bg );

		final Button ftux_check = new Button( ftux_check_area, SWT.CHECK );
		grid_data = new GridData();
		grid_data.horizontalIndent = 16;
		Utils.setLayoutData(ftux_check,  grid_data );
		ftux_check.setBackground(  ftux_light_bg );
		
		Label ftux_check_test = new Label( ftux_check_area, SWT.WRAP );
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		Utils.setLayoutData(ftux_check_test, grid_data);
		
		ftux_check_test.setBackground( ftux_light_bg );
		ftux_check_test.setText( info2_text );

		
			// bottom info
		
		final StyledText ftux_bottom = new StyledText( ftux_holder, SWT.READ_ONLY | SWT.WRAP | SWT.NO_FOCUS );
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 2;
		grid_data.horizontalIndent = 16;
		Utils.setLayoutData(ftux_bottom, grid_data);
		
		ftux_bottom.setBackground( ftux_light_bg );
		ftux_bottom.setFont( bold_font );
		ftux_bottom.setText( MessageText.getString( "azbuddy.dchat.ftux.footer" ) + " " );
		
		{
			int	start	= ftux_bottom.getText().length();
			
			String url 		= MessageText.getString( "faq.legal.url" );
			String url_text	= MessageText.getString( "label.more.dot" );
			
			ftux_bottom.append( url_text );
			
			StyleRange styleRange = new StyleRange();
			styleRange.start = start;
			styleRange.length = url_text.length();
			styleRange.foreground = Colors.blue;
			styleRange.underline = true;
			
			styleRange.data = url;
			
			ftux_bottom.setStyleRange( styleRange );
		}
		
		ftux_bottom.addListener(
				SWT.MouseUp, 
				new Listener()
				{
					public void handleEvent(Event event) {
						int offset = ftux_bottom.getOffsetAtLocation(new Point (event.x, event.y));
						StyleRange style = ftux_bottom.getStyleRangeAtOffset(offset);
						
						if ( style != null ){
							
							String url = (String)style.data;
							
							try{
								Utils.launch( new URL( url ));
								
							}catch( Throwable e ){
								
								Debug.out( e );
							}
						}
					}
				});
		
		Label ftux_line = new Label( ftux_holder, SWT.SEPARATOR | SWT.HORIZONTAL );
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 2;
		grid_data.verticalIndent = 4;
		Utils.setLayoutData(ftux_line,  grid_data ); 
		
		Composite ftux_button_area = new Composite( ftux_holder, SWT.NULL );
		layout = new GridLayout();
		layout.numColumns = 2;
		ftux_button_area.setLayout(layout);
		
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 2;
		Utils.setLayoutData(ftux_button_area,  grid_data );
		ftux_button_area.setBackground( Colors.white );
		
		Label filler = new Label( ftux_button_area, SWT.NULL );
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		Utils.setLayoutData(filler,  grid_data );
		filler.setBackground( Colors.white );
		
		final Button ftux_accept = new Button( ftux_button_area, SWT.PUSH );
		grid_data = new GridData();
		grid_data.horizontalAlignment = SWT.RIGHT;
		grid_data.widthHint = 60;
		Utils.setLayoutData(ftux_accept, grid_data);

		ftux_accept.setText( MessageText.getString( "label.accept" ));
		
		ftux_accept.setEnabled( false );
		
		ftux_accept.addSelectionListener(
			new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent e) {
					beta.setFTUXAccepted( true );
				}
			});
		
		ftux_check.addSelectionListener(
			new SelectionAdapter() {
					
				public void widgetSelected(SelectionEvent e) {
					ftux_accept.setEnabled( ftux_check.getSelection());
				}
		});
			// LOG panel
		
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 4;
		log_holder.setLayout(layout);
		//grid_data = new GridData(GridData.FILL_BOTH );
		//grid_data.horizontalSpan = 2;
		//Utils.setLayoutData(log_holder, grid_data);
		
		log = new StyledText(log_holder,SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP | SWT.NO_FOCUS );
		grid_data = new GridData(GridData.FILL_BOTH);
		grid_data.horizontalSpan = 1;
		//grid_data.horizontalIndent = 4;
		Utils.setLayoutData(log, grid_data);
		//log.setIndent( 4 );
		
		log.setEditable( false );

		log_holder.setBackground( log.getBackground());

		final Menu log_menu = new Menu( log );
		
		log.setMenu(  log_menu );
				
		log.addMenuDetectListener(
			new MenuDetectListener() {
				
				public void 
				menuDetected(
					MenuDetectEvent e ) 
				{
					e.doit = false;
					
					boolean	handled = false;
					
					for ( MenuItem mi: log_menu.getItems()){
						
						mi.dispose();
					}

					try{
						Point mapped = log.getDisplay().map( null, log, new Point( e.x, e.y ));
						
						int offset = log.getOffsetAtLocation( mapped );
						
						final StyleRange sr = log.getStyleRangeAtOffset(  offset );
												
						if ( sr != null ){

							Object data = sr.data;
							
							if ( data instanceof ChatParticipant ){
								
								ChatParticipant cp = (ChatParticipant)data;
								
								List<ChatParticipant> cps = new ArrayList<ChatParticipant>();
								
								cps.add( cp );
								
								buildParticipantMenu( log_menu, cps );
								
								handled = true;
																
							}else if ( data instanceof String ){
								
								String url_str = (String)sr.data;
								
								String str = url_str;
								
								if ( str.length() > 50 ){
									
									str = str.substring( 0, 50 ) + "...";
								}
								
									// magnet special case for anon chat
								
								if ( chat.isAnonymous() && url_str.toLowerCase( Locale.US ).startsWith( "magnet:" )){
									
									String[] magnet_uri = { url_str };
									
									Set<String> networks = UrlUtils.extractNetworks( magnet_uri );
									
									String i2p_only_uri = magnet_uri[0] + "&net=" + UrlUtils.encode( AENetworkClassifier.AT_I2P );
										
									String i2p_only_str = i2p_only_uri;
									
									if ( i2p_only_str.length() > 50 ){
										
										i2p_only_str = i2p_only_str.substring( 0, 50 ) + "...";
									}
									
									i2p_only_str = lu.getLocalisedMessageText( "azbuddy.dchat.open.i2p.magnet" ) + ": " + i2p_only_str;
									
									final MenuItem mi_open_i2p_vuze = new MenuItem( log_menu, SWT.PUSH );
									
									mi_open_i2p_vuze.setText( i2p_only_str);
									mi_open_i2p_vuze.setData( i2p_only_uri );

									mi_open_i2p_vuze.addSelectionListener(
										new SelectionAdapter() {
											
											public void 
											widgetSelected(
												SelectionEvent e ) 
											{
												String url_str = (String)mi_open_i2p_vuze.getData();
												
												if ( url_str != null ){
													
													TorrentOpener.openTorrent( url_str );
												}
											}
										});
									
									if ( networks.size() == 1 && networks.iterator().next() == AENetworkClassifier.AT_I2P ){
										
										// already done above
										
									}else{
										
										str = lu.getLocalisedMessageText( "azbuddy.dchat.open.magnet" ) + ": " + str;
										
										final MenuItem mi_open_vuze = new MenuItem( log_menu, SWT.PUSH );
										
										mi_open_vuze.setText( str);
										mi_open_vuze.setData( url_str );

										mi_open_vuze.addSelectionListener(
											new SelectionAdapter() {
												
												public void 
												widgetSelected(
													SelectionEvent e ) 
												{
													String url_str = (String)mi_open_vuze.getData();
													
													if ( url_str != null ){
														
														TorrentOpener.openTorrent( url_str );
													}
												}
											});
									}
								}else{
								
								
									str = lu.getLocalisedMessageText( "azbuddy.dchat.open.in.vuze" ) + ": " + str;
																		
									final MenuItem mi_open_vuze = new MenuItem( log_menu, SWT.PUSH );
									
									mi_open_vuze.setText( str);
									mi_open_vuze.setData( url_str );

									mi_open_vuze.addSelectionListener(
										new SelectionAdapter() {
											
											public void 
											widgetSelected(
												SelectionEvent e ) 
											{
												String url_str = (String)mi_open_vuze.getData();
												
												if ( url_str != null ){
													
													String lc_url_str = url_str.toLowerCase( Locale.US );
													
													if ( lc_url_str.startsWith( "chat:" )){
														
														try{
															beta.handleURI( url_str, true );
															
														}catch( Throwable f ){
															
															Debug.out( f );
														}
														
													}else{
													
														TorrentOpener.openTorrent( url_str );
													}
												}
											}
										});
								}
								
								final MenuItem mi_open_ext = new MenuItem( log_menu, SWT.PUSH );
								
								mi_open_ext.setText( lu.getLocalisedMessageText( "azbuddy.dchat.open.in.browser" ));
								
								mi_open_ext.addSelectionListener(
									new SelectionAdapter() {
										
										public void 
										widgetSelected(
											SelectionEvent e ) 
										{
											String url_str = (String)mi_open_ext.getData();
											
											Utils.launch( url_str );
										}
									});
								
								new MenuItem( log_menu, SWT.SEPARATOR );
								
								if ( chat.isAnonymous() && url_str.toLowerCase( Locale.US ).startsWith( "magnet:" )){

									String[] magnet_uri = { url_str };
									
									Set<String> networks = UrlUtils.extractNetworks( magnet_uri );
									
									String i2p_only_uri = magnet_uri[0] + "&net=" + UrlUtils.encode( AENetworkClassifier.AT_I2P );

									final MenuItem mi_copy_i2p_clip = new MenuItem( log_menu, SWT.PUSH );
									
									mi_copy_i2p_clip.setText( lu.getLocalisedMessageText( "azbuddy.dchat.copy.i2p.magnet" ));
									mi_copy_i2p_clip.setData( i2p_only_uri );

									mi_copy_i2p_clip.addSelectionListener(
											new SelectionAdapter() {
												
												public void 
												widgetSelected(
													SelectionEvent e ) 
												{
													String url_str = (String)mi_copy_i2p_clip.getData();
													
													if ( url_str != null ){
														
														ClipboardCopy.copyToClipBoard( url_str );
													}
												}
											});
									
									if ( networks.size() == 1 && networks.iterator().next() == AENetworkClassifier.AT_I2P ){
										
										// already done above
										
									}else{
										
										final MenuItem mi_copy_clip = new MenuItem( log_menu, SWT.PUSH );
										
										mi_copy_clip.setText( lu.getLocalisedMessageText( "azbuddy.dchat.copy.magnet" ));
										mi_copy_clip.setData( url_str );

										mi_copy_clip.addSelectionListener(
												new SelectionAdapter() {
													
													public void 
													widgetSelected(
														SelectionEvent e ) 
													{
														String url_str = (String)mi_copy_clip.getData();
														
														if ( url_str != null ){
															
															ClipboardCopy.copyToClipBoard( url_str );
														}
													}
												});
										
									}
								}else{
									
									final MenuItem mi_copy_clip = new MenuItem( log_menu, SWT.PUSH );
									
									mi_copy_clip.setText( lu.getLocalisedMessageText( "label.copy.to.clipboard" ));
									mi_copy_clip.setData( url_str );

									mi_copy_clip.addSelectionListener(
											new SelectionAdapter() {
												
												public void 
												widgetSelected(
													SelectionEvent e ) 
												{
													String url_str = (String)mi_copy_clip.getData();
													
													if ( url_str != null ){
														
														ClipboardCopy.copyToClipBoard( url_str );
													}
												}
											});
								}				
																
								if ( url_str.toLowerCase().startsWith( "http" )){
									
									mi_open_ext.setData( url_str );
									
									mi_open_ext.setEnabled( true );
									
								}else{
									
									mi_open_ext.setEnabled( false );
								}
																
								handled = true;
							}else{
								
								if ( Constants.isCVSVersion()){
									
									if ( sr instanceof MyStyleRange ){
										
										final MyStyleRange msr = (MyStyleRange)sr;
										
										MenuItem   item = new MenuItem( log_menu, SWT.NONE );

										item.setText( MessageText.getString( "label.copy.to.clipboard"));

										item.addSelectionListener(
											new SelectionAdapter()
											{
												@Override
												public void 
												widgetSelected(
													SelectionEvent e ) 
												{
													ClipboardCopy.copyToClipBoard( msr.message.getMessage());
												}
											});
										
										handled = true;
									}
								}
							}
						}
					}catch( Throwable f ){
					}
					
					if ( !handled ){
						
						final String text = log.getSelectionText();
						
						if ( text != null && text.length() > 0 ){
							
							MenuItem   item = new MenuItem( log_menu, SWT.NONE );

							item.setText( MessageText.getString( "label.copy.to.clipboard"));

							item.addSelectionListener(
								new SelectionAdapter()
								{
									@Override
									public void 
									widgetSelected(
										SelectionEvent e ) 
									{
										ClipboardCopy.copyToClipBoard( text );
									}
								});
							
							handled = true;
						}
					}
					
					if ( handled ){
						
						e.doit = true;
					}
				}
			});
		
		log.addListener(
			SWT.MouseDoubleClick,
			new Listener()
			{
				public void 
				handleEvent(
					Event e )
				{
					try{
						final int offset = log.getOffsetAtLocation( new Point( e.x, e.y ) );
						
						for ( int i=0;i<log_styles.length;i++){
							
							StyleRange sr = log_styles[i];
							
							Object data = sr.data;

							if ( data != null && offset >= sr.start && offset < sr.start + sr.length ){
								
								boolean anon_chat = chat.isAnonymous();
								
								if ( data instanceof String ){
									
									final String	url_str = (String)data;
									
									String lc_url_str = url_str.toLowerCase( Locale.US );
									
									if ( lc_url_str.startsWith( "chat:" )){
										
											// no double-click support for anon->public chats
										
										if ( anon_chat && !lc_url_str.startsWith( "chat:anon:" )){
											
											return;
										}
										
										try{
											beta.handleURI( url_str, true );
											
										}catch( Throwable f ){
											
											Debug.out( f );
										}
									}else{
										
											// no double-click support for anon->public urls
										
										if ( anon_chat ){
											
											try{
												String host = new URL( lc_url_str ).getHost();
												
													// note that magnet-uris are always decoded here as public, which is what we want mostly
												
												if ( AENetworkClassifier.categoriseAddress( host ) == AENetworkClassifier.AT_PUBLIC ){
		
													return;
	
												}
											}catch( Throwable f ){
												
												return;
											}
										}
										
										if ( 	lc_url_str.contains( ".torrent" ) || 
												UrlUtils.parseTextForMagnets( url_str ) != null ){
											
											TorrentOpener.openTorrent( url_str );
											
										}else{
											
											if ( url_str.toLowerCase( Locale.US ).startsWith( "http" )){
												
													// without this backoff we end up with the text widget
													// being left in a 'mouse down' state when returning to it :(
												
												Utils.execSWTThreadLater(
													100, 
													new Runnable() 
													{
														public void 
														run()
														{	
															Utils.launch( url_str );
														}
													});
											}else{
												
												TorrentOpener.openTorrent( url_str );
											}
										}
									}
									
									log.setSelection( offset );
									
									e.doit = false;
									
								}else if ( data instanceof ChatParticipant ){
									
									ChatParticipant participant = (ChatParticipant)data;
									
									String name = participant.getName( true );
									
									String existing = input_area.getText();
									
									if ( existing.length() > 0 && !existing.endsWith( " " )){
										
										name = " " + name;
									}
									
									input_area.append( name );
								}
							}
						}
					}catch( Throwable f ){
						
					}
				}
			});

		log.addMouseTrackListener(
			new MouseTrackListener() {
				
				private StyleRange		old_range;
				private StyleRange		temp_range;
				private int				temp_index;
				
				public void mouseHover(MouseEvent e) {
					
					boolean active = false;

					try{
						int offset = log.getOffsetAtLocation( new Point( e.x, e.y ) );
						
						for ( int i=0;i<log_styles.length;i++){
							
							StyleRange sr = log_styles[i];
							
							Object data = sr.data;

							if ( data != null && offset >= sr.start && offset < sr.start + sr.length ){
									
								if ( old_range != null  ){
									
									if ( 	temp_index < log_styles.length &&
											log_styles[temp_index] == temp_range ){
										
										log_styles[ temp_index ] = old_range;
																				
										old_range	= null;
									}
								}
								
								sr = log_styles[i];
								
								String tt_extra = "";
								
								if ( data instanceof String ){
									
									try{
										URL url = new URL((String)data);
										
										String query = url.getQuery();
										
										if ( query != null ){
											
											String[] bits = query.split( "&" );
											
											int seeds 		= -1;
											int leechers	= -1;
											
											for ( String bit: bits ){
												
												String[] temp = bit.split( "=" );
												
												String lhs = temp[0];
												
												if ( lhs.equals( "_s" )){
													
													seeds = Integer.parseInt( temp[1] );
													
												}else if ( lhs.equals( "_l" )){
													
													leechers = Integer.parseInt( temp[1] );
												}
											}
											
											if ( seeds != -1 && leechers != -1){
												
												tt_extra = ": seeds=" + seeds +", leechers=" + leechers;
											}
										}
									}catch( Throwable f ){
									}
								}
								
								log.setToolTipText( MessageText.getString( "label.right.click.for.options" ) + tt_extra );
								
									
								StyleRange derp;
								
								if ( sr instanceof MyStyleRange ){
									
									derp = new MyStyleRange((MyStyleRange)sr );
								}else{
									
									derp = new StyleRange( sr );
								}
									
								derp.start = sr.start;
								derp.length = sr.length;
								
								derp.borderStyle = SWT.BORDER_DASH;
								
								old_range	= sr;
								temp_range	= derp;
								temp_index	= i;
								
								log_styles[i] = derp;
								
								log.setStyleRanges( log_styles );
								
								active = true;
									
								break;
							}
						}
					}catch( Throwable f ){
				
					}
					
					if ( !active ){
						
						log.setToolTipText( "" );
						
						if ( old_range != null ){
							
							if ( 	temp_index < log_styles.length &&
									log_styles[temp_index] == temp_range ){
								
								log_styles[ temp_index ] = old_range;
								
								old_range	= null;
								
								log.setStyleRanges( log_styles );
							}
						}
					}
				}
				
				public void mouseExit(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				public void mouseEnter(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});

		
		log.addKeyListener(
			new KeyAdapter()
			{
				public void 
				keyPressed(
					KeyEvent event ) 
				{
					int key = event.character;
					
					if ( key <= 26 && key > 0 ){
						
						key += 'a' - 1;
					}

					if ( key == 'a' && event.stateMask == SWT.MOD1 ){
						
						event.doit = false;
						
						log.selectAll();
					}
				}
			});
		
		Composite rhs = new Composite(sash, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 4;
		layout.marginRight = 4;
		rhs.setLayout(layout);
		grid_data = new GridData(GridData.FILL_VERTICAL );
		int rhs_width=Constants.isWindows?150:160;
		grid_data.widthHint = rhs_width;
		Utils.setLayoutData(rhs, grid_data);

			// options
		
		Composite top_right = new Composite(rhs, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		top_right.setLayout(layout);
		grid_data = new GridData( GridData.FILL_HORIZONTAL );
		//grid_data.heightHint = 50;
		Utils.setLayoutData(top_right, grid_data);
		
		boolean	can_popout = shell == null && public_chat;

		Label label = new Label( top_right, SWT.NULL );
		grid_data = new GridData( GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan=can_popout?1:2;
		Utils.setLayoutData(label, grid_data);
		
		LinkLabel link = new LinkLabel( top_right, "label.help", lu.getLocalisedMessageText( "azbuddy.dchat.link.url" ));	
		//grid_data.horizontalAlignment = SWT.END;
		//link.getlabel().setLayoutData( grid_data );

		if ( can_popout ){
			
			Label pop_out = new Label( top_right, SWT.NULL );
			image = ImageLoader.getInstance().getImage( "popout_window" );
			pop_out.setImage( image );
			grid_data = new GridData();
			grid_data.widthHint=image.getBounds().width;
			grid_data.heightHint=image.getBounds().height;
			Utils.setLayoutData(pop_out, grid_data);
			
			pop_out.setCursor(label.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			
			pop_out.setToolTipText( MessageText.getString( "label.pop.out" ));
			
			pop_out.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent arg0) {
					try{
						createChatWindow( view, plugin, chat.getClone(), true );
						
					}catch( Throwable e ){
						
						Debug.out( e);
					}
				}
			});
				
		}	
		
			// nick name
		
		Composite nick_area = new Composite(top_right, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		if ( !Constants.isWindows ){
			layout.horizontalSpacing = 2;
			layout.verticalSpacing = 2;
		}
		nick_area.setLayout(layout);
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan=3;
		Utils.setLayoutData(nick_area, grid_data);
		
		label = new Label( nick_area, SWT.NULL );
		label.setText( lu.getLocalisedMessageText( "azbuddy.dchat.nick" ));
		grid_data = new GridData();
		//grid_data.horizontalIndent=4;
		Utils.setLayoutData(label, grid_data);

		nickname = new Text( nick_area, SWT.BORDER );
		grid_data = new GridData( GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan=1;
		Utils.setLayoutData(nickname,  grid_data );

		nickname.setText( chat.getNickname( false ));
		nickname.setMessage( chat.getDefaultNickname());
		
		label = new Label( nick_area, SWT.NULL );
		label.setText( lu.getLocalisedMessageText( "label.shared" ));
		label.setToolTipText( lu.getLocalisedMessageText( "azbuddy.dchat.shared.tooltip" ));
		
		shared_nick_button = new Button( nick_area, SWT.CHECK );
		
		shared_nick_button.setSelection( chat.isSharedNickname());

		shared_nick_button.addSelectionListener(
			new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) {
					
					boolean shared = shared_nick_button.getSelection();
					
					chat.setSharedNickname( shared );
				}
			});
		
		nickname.addListener(SWT.FocusOut, new Listener() {
	        public void handleEvent(Event event) {
	        	String nick = nickname.getText().trim();
	        	
	        	if ( chat.isSharedNickname()){
	        		
	        		if ( chat.getNetwork() == AENetworkClassifier.AT_PUBLIC ){
	        		
	        			beta.setSharedPublicNickname( nick );
	        			
	        		}else{
	        			
	        			beta.setSharedAnonNickname( nick );
	        		}
	        	}else{
	        		
	        		chat.setInstanceNickname( nick );
	        	}
	        }
	    });
				
		
		table_header = new BufferedLabel( top_right, SWT.DOUBLE_BUFFERED );
		grid_data = new GridData( GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan=3;
		if ( !Constants.isWindows ){
			grid_data.horizontalIndent = 2;
		}
		Utils.setLayoutData(table_header,  grid_data );
		table_header.setText(MessageText.getString( "PeersView.state.pending" ));
		
			// table
		
		buddy_table = new Table(rhs, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);

		String[] headers = { 
				"azbuddy.ui.table.name" };

		int[] sizes = { rhs_width-10 };

		int[] aligns = { SWT.LEFT };

		for (int i = 0; i < headers.length; i++){

			TableColumn tc = new TableColumn(buddy_table, aligns[i]);
				
			tc.setWidth(Utils.adjustPXForDPI(sizes[i]));

			Messages.setLanguageText(tc, headers[i]);
		}	

	    buddy_table.setHeaderVisible(true);

	    grid_data = new GridData(GridData.FILL_BOTH);
	    // grid_data.heightHint = buddy_table.getHeaderHeight() * 3;
		Utils.setLayoutData(buddy_table, grid_data);
		
		
		buddy_table.addListener(
			SWT.SetData,
			new Listener()
			{
				public void 
				handleEvent(
					Event event) 
				{
					TableItem item = (TableItem)event.item;
					
					setItemData( item );
				}
			});
		
		final Menu menu = new Menu(buddy_table);
		
		buddy_table.setMenu( menu );
		
		menu.addMenuListener(
			new MenuListener() 
			{
				public void 
				menuShown(
					MenuEvent e ) 
				{
					MenuItem[] items = menu.getItems();
					
					for (int i = 0; i < items.length; i++){
						
						items[i].dispose();
					}

					final TableItem[] selection = buddy_table.getSelection();
					
					List<ChatParticipant>	participants = new ArrayList<BuddyPluginBeta.ChatParticipant>( selection.length );
					
					for (int i=0;i<selection.length;i++){
						
						TableItem item = selection[i];
						
						ChatParticipant	participant = (ChatParticipant)item.getData();

						if ( participant == null ){
							
								// item data won't be set yet for items that haven't been
								// visible...
							
							participant = setItemData( item );
						}
						
						if ( participant != null ){
						
							participants.add( participant );
						}
					}
					
					buildParticipantMenu( menu, participants );
				}
				
				public void menuHidden(MenuEvent e) {
				}
			});
		
		buddy_table.addKeyListener(
				new KeyAdapter()
				{
					public void 
					keyPressed(
						KeyEvent event ) 
					{
						int key = event.character;
						
						if ( key <= 26 && key > 0 ){
							
							key += 'a' - 1;
						}

						if ( key == 'a' && event.stateMask == SWT.MOD1 ){
							
							event.doit = false;
							
							buddy_table.selectAll();
						}
					}
				});
		
		
		buddy_table.addMouseListener(
			new MouseAdapter()
			{
				public void 
				mouseDoubleClick(
					MouseEvent e ) 
				{
					TableItem[] selection = buddy_table.getSelection();
					
					if ( selection.length != 1 ){
						
						return;
					}
					
					TableItem item = selection[0];
						
					ChatParticipant	participant = (ChatParticipant)item.getData();
					
					String name = participant.getName( true );
					
					String existing = input_area.getText();
					
					if ( existing.length() > 0 && !existing.endsWith( " " )){
						
						name = " " + name;
					}
					
					input_area.append( name );

				}
			});
		
	    Utils.maintainSashPanelWidth( sash, rhs, new int[]{ 700, 300 }, "azbuddy.dchat.ui.sash.pos" );
	    
	    /*
	    Listener sash_listener= 
	    	new Listener()
	    	{
	    		private int	lhs_weight;
	    		private int	lhs_width;
	    		
		    	public void 
				handleEvent(
					Event ev ) 
				{
		    		if ( ev.widget == lhs ){
		    			
		    			int[] weights = sash.getWeights();
		    			
		    			
		    			if ( lhs_weight != weights[0] ){
		    				
		    					// sash has moved
		    				
		    				lhs_weight = weights[0];
		    				
		    					// keep track of the width
		    				
		    				lhs_width = lhs.getBounds().width;
		    			}
		    		}else{
		    			
		    				// resize
		    			
		    			if ( lhs_width > 0 ){
		    						    				
				            int width = sash.getClientArea().width;
				            	
				            double ratio = (double)lhs_width/width;
	
				            lhs_weight = (int)(ratio*1000 );
				          
				            sash.setWeights( new int[]{ lhs_weight, 1000 - lhs_weight });
		    			}
		    		}
			    }
		    };
	    
	    lhs.addListener(SWT.Resize, sash_listener );
	    sash.addListener(SWT.Resize, sash_listener );
	    */
	    
	    	// bottom area
	    
	    Composite bottom_area = new Composite( parent, SWT.NULL );
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		bottom_area.setLayout(layout);

		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 2;
		bottom_area.setLayoutData( grid_data );

			// Text
		
	    
		input_area = new Text( bottom_area, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.horizontalSpan = 1;
		grid_data.heightHint = 30;
		grid_data.horizontalIndent = 4;
		Utils.setLayoutData(input_area, grid_data);
			
		input_area.setTextLimit( MAX_MSG_LENGTH );
		
		input_area.addKeyListener(
			new KeyListener()
			{
				private LinkedList<String>	history 	= new LinkedList<String>();
				private int					history_pos	= -1;
				
				private String				buffered_message = "";
				
				public void 
				keyPressed(
					KeyEvent e) 
				{
					if ( e.keyCode == SWT.CR ){
				
						e.doit = false;
						
						String message = input_area.getText().trim();
						
						if ( message.length() > 0 ){
							
							sendMessage(  message );
							
							history.addFirst( message );
							
							if ( history.size() > 32 ){
								
								history.removeLast();
							}
							
							history_pos = -1;
							
							buffered_message = "";
							
							input_area.setText( "" );
						}
					}else if ( e.keyCode == SWT.ARROW_UP ){
							
						history_pos++;
						
						if ( history_pos < history.size()){
						
							if ( history_pos == 0 ){
								
								buffered_message = input_area.getText().trim();
							}

							String msg = history.get( history_pos );
							
							input_area.setText( msg );
							
							input_area.setSelection( msg.length());
							
						}else{
							
							history_pos = history.size() - 1;
						}
						
						e.doit = false;
						
					}else if ( e.keyCode == SWT.ARROW_DOWN ){

						history_pos--;

						if ( history_pos >= 0 ){
													
							String msg = history.get( history_pos );
							
							input_area.setText( msg );
							
							input_area.setSelection( msg.length());
							
						}else{
								
							if ( history_pos == -1 ){
								
								input_area.setText( buffered_message );
								
								if ( buffered_message.length() > 0 ){
	
									input_area.setSelection( buffered_message.length());
								
									buffered_message = "";
								}
							}else{
								
								history_pos = -1;
							}
						}
						
						e.doit = false;
						
					}else{
						
						if ( e.stateMask == SWT.MOD1 ){

							int key = e.character;
							
							if ( key <= 26 && key > 0 ){
								
								key += 'a'-1;
							}

							if ( key == 'a' ){
								
								input_area.selectAll();
							}
						}
					}
				}
				
				public void 
				keyReleased(
					KeyEvent e ) 
				{
				}
			});
		
		Composite button_area = new Composite( bottom_area, SWT.NULL );
		
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginRight = 4;
		button_area.setLayout(layout);

		rss_button = new Button( button_area, SWT.PUSH );
		Image rss_image = ImageLoader.getInstance().getImage("image.sidebar.subscriptions");
		rss_button.setImage(rss_image);
		grid_data = new GridData(GridData.FILL_HORIZONTAL );
		grid_data.widthHint = rss_image.getBounds().width;
		grid_data.heightHint = rss_image.getBounds().height;
		rss_button.setLayoutData(grid_data);
		//rss_button.setEnabled(false);
		
		rss_button.setToolTipText( MessageText.getString( "azbuddy.dchat.rss.subscribe.info"));
		
		rss_button.addSelectionListener(
			new SelectionAdapter(){
				
				public void 
				widgetSelected(
					SelectionEvent ev )
				{
					try{
						String url = "azplug:?id=azbuddy&arg=" + UrlUtils.encode( chat.getURL() + "&format=rss");
						
						SubscriptionManager sm = PluginInitializer.getDefaultInterface().getUtilities().getSubscriptionManager();
	
						Map<String,Object>	options = new HashMap<String, Object>();
						
						if ( chat.isAnonymous()){
														
							options.put( SubscriptionManager.SO_ANONYMOUS, true );
						}
						
						options.put( SubscriptionManager.SO_NAME, chat.getName());
						
						sm.requestSubscription( new URL( url ), options );
						
					}catch( Throwable e ){
						
						Debug.out( e );
					}
				}

			});
		
		ftux_ok = beta.getFTUXAccepted();
		
		if ( chat.isReadOnly()){
		
			input_area.setText( MessageText.getString( "azbuddy.dchat.ro" ));
					
			input_area.setEnabled( false );
		
		}else if ( !ftux_ok ){
			
			input_area.setEnabled( false );
			
		}else{
		
			input_area.setFocus();
		}
		
		final boolean[] ftux_init_done = { false };
		
		beta.addFTUXStateChangeListener(
			new FTUXStateChangeListener()
			{
				public void
				stateChanged(
					final boolean		_ftux_ok )
				{
					if ( ftux_stack.isDisposed()){
						
						beta.removeFTUXStateChangeListener( this );
						
					}else{
						
						Utils.execSWTThread(
							new Runnable()
							{
								
								public void 
								run()
								{
									ftux_ok = _ftux_ok;
									
									stack_layout.topControl = ftux_ok?log_holder:ftux_holder;
									
									if ( ftux_init_done[0]){
									
										ftux_stack.layout( true, true );
									}
									
									if ( !chat.isReadOnly()){
										
										input_area.setEnabled( ftux_ok );
									}
									
									table_resort_required = true;
									
									updateTable( false );
								}
							});
					}
				}
			});

		if ( !chat.isReadOnly()){
			
			drop_targets = new DropTarget[]{
				new DropTarget(log, DND.DROP_COPY),
				new DropTarget(input_area, DND.DROP_COPY)
			};
			
			for ( DropTarget drop_target: drop_targets ){
				
				drop_target.setTransfer(new Transfer[] {
					URLTransfer.getInstance(),
					FileTransfer.getInstance(),
					TextTransfer.getInstance(),
				});
		
				drop_target.addDropListener(new DropTargetAdapter() {
					public void dropAccept(DropTargetEvent event) {
						event.currentDataType = URLTransfer.pickBestType(event.dataTypes,
								event.currentDataType);
					}
		
					public void dragEnter(DropTargetEvent event) {
					}
		
					public void dragOperationChanged(DropTargetEvent event) {
					}
		
					public void dragOver(DropTargetEvent event) {
	
						if ((event.operations & DND.DROP_LINK) > 0)
							event.detail = DND.DROP_LINK;
						else if ((event.operations & DND.DROP_COPY) > 0)
							event.detail = DND.DROP_COPY;
						else if ((event.operations & DND.DROP_DEFAULT) > 0)
							event.detail = DND.DROP_COPY;
		
		
						event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
					}
		
					public void dragLeave(DropTargetEvent event) {
					}
		
					public void drop(DropTargetEvent event) {
						handleDrop( event.data );				
					}
				});
			}
		}
		
		ftux_init_done[0] = true;
		
		Control[] focus_controls = { log, input_area, buddy_table, nickname, shared_nick_button };
		
		Listener focus_listener = new Listener() {
			
			public void handleEvent(Event event) {
				activate();
			}
		};
		
		for ( Control c: focus_controls ){
			
			c.addListener( SWT.FocusIn, focus_listener );
		}

		BuddyPluginBeta.ChatParticipant[] existing_participants = chat.getParticipants();
		
		synchronized( participants ){
			
			participants.addAll( Arrays.asList( existing_participants ));
		}
		
		table_resort_required = true;
		
		updateTable( false );
		
		BuddyPluginBeta.ChatMessage[] history = chat.getHistory();
				
		logChatMessages( history );
		
		chat.addListener( this );
		
		build_complete	= true;
		
		if ( can_popout && !ftux_ok && !auto_ftux_popout_done ){
			
			auto_ftux_popout_done = true;
			
			try{
				createChatWindow( view, plugin, chat.getClone(), true );
				
			}catch( Throwable e ){
				
			}
		}
	}
	
	private void
	addFriendsMenu(
		Menu		menu )
	{
		if ( chat.isAnonymous()){
			
			return;
		}
		
		final Menu friends_menu = new Menu(menu.getShell(), SWT.DROP_DOWN);
		MenuItem friends_menu_item = new MenuItem( menu, SWT.CASCADE);
		friends_menu_item.setMenu(friends_menu);
		friends_menu_item.setText(  MessageText.getString( "Views.plugins.azbuddy.title" ));
		
		friends_menu.addMenuListener(
			new MenuAdapter() 
			{
				public void 
				menuShown(
					MenuEvent e ) 
				{
					MenuItem[] items = friends_menu.getItems();
					
					for (int i = 0; i < items.length; i++){
						
						items[i].dispose();
					}
					
					boolean	enabled = plugin.isClassicEnabled();
					
					if ( enabled ){
						
						MenuItem mi = new MenuItem( friends_menu, SWT.PUSH );
						mi.setText( MessageText.getString( "azbuddy.insert.friend.key" ));
				
						mi.addSelectionListener(
								new SelectionAdapter() {				
									public void 
									widgetSelected(
										SelectionEvent event ) 
									{								
										String key = plugin.getPublicKey();
										
										String uri = "chat:friend:?key=" + key;
										
										String my_nick = chat.getNickname( false );
										
										if ( my_nick.length() > 0 ){
											
											uri += "[[" + UrlUtils.encode( "Friend Key for " + my_nick ) + "]]";
										}
										
										input_area.append( uri  );
									}
								});
							
						new MenuItem(friends_menu, SWT.SEPARATOR );

						mi = new MenuItem( friends_menu, SWT.PUSH );
						mi.setText( MessageText.getString( "azbuddy.view.friends" ));
				
						mi.addSelectionListener(
								new SelectionAdapter() {				
									public void 
									widgetSelected(
										SelectionEvent event ) 
									{								
										view.selectClassicTab();
									}
								});
						
					}else{
						
						MenuItem mi = new MenuItem( friends_menu, SWT.PUSH );
						mi.setText( MessageText.getString( "devices.contextmenu.od.enable" ));
				
						mi.addSelectionListener(
								new SelectionAdapter() {				
									public void 
									widgetSelected(
										SelectionEvent event ) 
									{								
										plugin.setClassicEnabled( true );
									}
								});
						
					}
				}
			});
	}
	
	private void
	buildParticipantMenu(
		final Menu					menu,
		final List<ChatParticipant>	participants )
	{
		boolean	can_ignore 	= false;
		boolean	can_listen	= false;
		boolean	can_pin		= false;
		boolean	can_unpin	= false;
		
		boolean	can_spam	= false;
		boolean	can_unspam	= false;
				
		for ( ChatParticipant participant: participants ){
						
			if ( DEBUG_ENABLED ){
				
				System.out.println( participant.getName() + "/" + participant.getAddress());
				
				List<ChatMessage>	messages = participant.getMessages();
				
				for ( ChatMessage msg: messages ){
					
					System.out.println( "    " + msg.getTimeStamp() + ", " + msg.getAddress() + " - " + msg.getMessage());
				}
			}
			
			if ( participant.isIgnored()){
			
				can_listen = true;
				
			}else{
				
				can_ignore = true;
			}
			
			if ( participant.isPinned()){
				
				can_unpin = true;
				
			}else{
				
				if ( !participant.isMe()){
				
					can_pin = true;
				}
			}
			
			if ( participant.isSpammer()){
				
				can_unspam = true;
				
			}else{
				
				can_spam |= participant.canSpammer();
			}
		}
		
		final MenuItem ignore_item = new MenuItem(menu, SWT.PUSH);
		
		ignore_item.setText( lu.getLocalisedMessageText( "label.mute" ) );

		ignore_item.addSelectionListener(
			new SelectionAdapter() 
			{
				public void 
				widgetSelected(
					SelectionEvent e) 
				{
					boolean	changed = false;
					
					for ( ChatParticipant participant: participants ){
						
						if ( !participant.isIgnored()){
							
							participant.setIgnored( true );
							
							setProperties( participant );
							
							changed = true;
						}
					}
					
					if ( changed ){
						
						messagesChanged();
					}
				};
			});
		
		ignore_item.setEnabled( can_ignore );
		
		final MenuItem listen_item = new MenuItem(menu, SWT.PUSH);
		
		listen_item.setText(lu.getLocalisedMessageText( "label.listen" ) );

		listen_item.addSelectionListener(
			new SelectionAdapter() 
			{
				public void 
				widgetSelected(
					SelectionEvent e) 
				{
					boolean	changed = false;
					
					for ( ChatParticipant participant: participants ){
						
						if ( participant.isIgnored()){
							
							participant.setIgnored( false );
							
							setProperties( participant );
							
							changed = true;
						}
					}
					
					if ( changed ){
						
						messagesChanged();
					}
				};
			});
		
		listen_item.setEnabled( can_listen );
		
			// spam
		
		final MenuItem spam_item = new MenuItem(menu, SWT.PUSH);
		
		spam_item.setText(lu.getLocalisedMessageText( "label.spam" ) );

		spam_item.addSelectionListener(
			new SelectionAdapter() 
			{
				public void 
				widgetSelected(
					SelectionEvent e) 
				{
					boolean	changed = false;
					
					for ( ChatParticipant participant: participants ){
						
						if ( participant.canSpammer()){
							
							participant.setSpammer( true );
							
							setProperties( participant );
							
							changed = true;
						}
					}
					
					if ( changed ){
						
						messagesChanged();
					}
				};
			});
		
		spam_item.setEnabled( can_spam );
		
		final MenuItem unspam_item = new MenuItem(menu, SWT.PUSH);
		
		unspam_item.setText(lu.getLocalisedMessageText( "label.not.spam" ) );

		unspam_item.addSelectionListener(
			new SelectionAdapter() 
			{
				public void 
				widgetSelected(
					SelectionEvent e) 
				{
					boolean	changed = false;
					
					for ( ChatParticipant participant: participants ){
						
						if ( participant.isSpammer()){
							
							participant.setSpammer( false );
							
							setProperties( participant );
							
							changed = true;
						}
					}
					
					if ( changed ){
						
						messagesChanged();
					}
				};
			});
		
		unspam_item.setEnabled( can_unspam );
		
			// pin
		
		new MenuItem(menu, SWT.SEPARATOR );
		
		final MenuItem pin_item = new MenuItem(menu, SWT.PUSH);
		
		pin_item.setText( lu.getLocalisedMessageText( "label.pin" ) );

		pin_item.addSelectionListener(
			new SelectionAdapter() 
			{
				public void 
				widgetSelected(
					SelectionEvent e) 
				{
					for ( ChatParticipant participant: participants ){
						
						if ( !participant.isPinned()){
							
							if ( !participant.isMe()){
								
								participant.setPinned( true );
								
								setProperties( participant );
							}
						}
					}
				};
			});
		
		pin_item.setEnabled( can_pin );
		
		final MenuItem unpin_item = new MenuItem(menu, SWT.PUSH);
		
		unpin_item.setText( lu.getLocalisedMessageText( "label.unpin" ) );

		unpin_item.addSelectionListener(
			new SelectionAdapter() 
			{
				public void 
				widgetSelected(
					SelectionEvent e) 
				{
					for ( ChatParticipant participant: participants ){
						
						if ( participant.isPinned()){
							
							participant.setPinned( false );
							
							setProperties( participant );
						}
					}
				};
			});
		
		unpin_item.setEnabled( can_unpin );
		
		if ( !chat.isPrivateChat()){
			
			new MenuItem(menu, SWT.SEPARATOR );
			
			final MenuItem private_chat_item = new MenuItem(menu, SWT.PUSH);
			
			private_chat_item.setText( lu.getLocalisedMessageText( "label.private.chat" ) );

			final byte[]	chat_pk = chat.getPublicKey();

			private_chat_item.addSelectionListener(
				new SelectionAdapter() 
				{
					public void 
					widgetSelected(
						SelectionEvent e) 
					{
						for ( ChatParticipant participant: participants ){
							
							if ( TEST_LOOPBACK_CHAT || !Arrays.equals( participant.getPublicKey(), chat_pk )){
								
								try{
									ChatInstance chat = participant.createPrivateChat();
								
									createChatWindow( view, plugin, chat);
									
								}catch( Throwable f ){
									
									Debug.out( f );
								}
							}
						}
					};
				});
				
			boolean	pc_enable = false;
			
			if ( chat_pk != null ){
				
				for ( ChatParticipant participant: participants ){
					
					if ( !Arrays.equals( participant.getPublicKey(), chat_pk )){
						
						pc_enable = true;
					}
				}
			}
			
			private_chat_item.setEnabled( pc_enable || TEST_LOOPBACK_CHAT );
		}
		
		if ( participants.size() == 1 ){
			
			new MenuItem(menu, SWT.SEPARATOR );
	
			final MenuItem mi_copy_clip = new MenuItem( menu, SWT.PUSH );
			
			mi_copy_clip.setText( lu.getLocalisedMessageText( "label.copy.to.clipboard" ));
	
			mi_copy_clip.addSelectionListener(
				new SelectionAdapter() {
					
					public void 
					widgetSelected(
						SelectionEvent e ) 
					{
						StringBuffer sb = new StringBuffer();
						
						sb.append( participants.get(0).getName( true ));
						
						if ( Constants.isCVSVersion()){
							
							List<ChatMessage>	messages = participants.get(0).getMessages();
							
							for ( ChatMessage msg: messages ){
								
								sb.append( "\r\n" + msg.getMessage());
							}

						}
						
						ClipboardCopy.copyToClipBoard( sb.toString());
					}
				});
		}
	}
	
	private ChatParticipant
	setItemData(
		TableItem		item )
	{
		int index = buddy_table.indexOf(item);
		
		if ( index < 0 || index >= participants.size()){
			
			return( null );
		}
		
		ChatParticipant	participant = (BuddyPluginBeta.ChatParticipant)participants.get(index);
		
		item.setData( participant );
		
		item.setText(0, participant.getName( ftux_ok ));		
		
		setProperties( item, participant );	
		
		return( participant );
	}
	
	private void
	setProperties(
		ChatParticipant		p )
	{
		for ( TableItem ti: buddy_table.getItems()){
			
			if ( ti.getData() == p ){
				
				setProperties( ti, p );
			}
		}
	}
	
	private void
	setProperties(
		TableItem			item,
		ChatParticipant		p )
	{
		if ( p.isIgnored() || p.isSpammer()){
		
			item.setForeground( 0, Colors.grey );
			
		}else{
			
			if ( p.isPinned()){
			
				item.setForeground( 0, Colors.fadedGreen );
				
			}else{
			
				if ( p.isMe()){
					
					item.setForeground( 0, Colors.fadedGreen );
					
					item.setFont( 0, italic_font );
					
				}else if ( p.isNickClash()){
					
					item.setForeground( 0, Colors.red );
					
				}else{
					
					if ( p.hasNickname()){
						
						item.setForeground( 0, Colors.blues[Colors.FADED_DARKEST] );
						
					}else{
						
						item.setForeground( 0, Colors.black );
					}
				}
			}
		}
	}
	
	protected void
	addDisposeListener(
		final DisposeListener	listener )
	{
		if ( shell != null ){
			
			if ( shell.isDisposed()){
				
				listener.widgetDisposed( null );
				
			}else{
									
				shell.addDisposeListener( listener );
			}
		}
	}
	
	private void
	updateTableHeader()
	{
		int	active 	= buddy_table.getItemCount();
		int online	= chat.getEstimatedNodes();
		
		String msg = 
			lu.getLocalisedMessageText( 
				"azbuddy.dchat.user.status",
					new String[]{
						online >=100?"100+":String.valueOf( online ),
						String.valueOf( active )
					});
			
		table_header.setText( msg );
	}
	
	protected void
	updateTable(
		boolean	async )
	{
		if ( async ){
			
			if ( !buddy_table.isDisposed()){

				Utils.execSWTThread(
					new Runnable()
					{
						public void
						run()
						{
							if ( buddy_table.isDisposed()){

								return;
							}
							
							updateTable( false );
							
							updateTableHeader();
						}
					});
			}					
		}else{
			
			if ( table_resort_required ){
				
				table_resort_required = false;
				
				sortParticipants();
			}
			
			buddy_table.setItemCount( participants.size());
			buddy_table.clearAll();
			buddy_table.redraw();
		}
	}
	
	public void
	handleExternalDrop(
		String	payload )
	{
		handleDrop( payload );
	}
	
	private void
	handleDrop(
		Object	payload )
	{
		if ( payload instanceof String[]){
			
			String[]	files = (String[])payload;
			
			if ( files.length == 0 ){
				
				Debug.out( "Nothing to drop" );
				
			}else{
				int hits = 0;
				
				for ( String file: files ){
				
					File f = new File( file );
	
					if ( f.exists()){
					
						dropFile( f );
						
						hits++;
					}
				}
				
				if ( hits == 0 ){
					
					Debug.out( "Nothing files found to drop" );
				}
			}
		}else if ( payload instanceof String ){
			
			String stuff = (String)payload;
			
			if ( stuff.startsWith( "DownloadManager\n" ) ||stuff.startsWith( "DiskManagerFileInfo\n" )){
				
				String[]	bits =  Constants.PAT_SPLIT_SLASH_N.split(stuff);
				
				for (int i=1;i<bits.length;i++){
					
					String	hash_str = bits[i];
					
					int	pos = hash_str.indexOf(';');
					
					try{

						if ( pos == -1 ){
							
							byte[]	 hash = Base32.decode( bits[i] );
			
							Download download = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface().getShortCuts().getDownload(hash);
							
							dropDownload( download );
										
						}else{
							
							String[] files = hash_str.split(";");
							
							byte[]	 hash = Base32.decode( files[0].trim());
							
							DiskManagerFileInfo[] dm_files = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface().getShortCuts().getDownload(hash).getDiskManagerFileInfo();
							
							for (int j=1;j<files.length;j++){
								
								DiskManagerFileInfo dm_file = dm_files[Integer.parseInt(files[j].trim())];
								
								dropDownloadFile( dm_file );
							}
						}
					}catch( Throwable e ){
						
						Debug.out( "Failed to get download for hash " + bits[1] );
					}
				}
			}else if ( stuff.startsWith( "TranscodeFile\n" )){
				
				String[]	bits =  Constants.PAT_SPLIT_SLASH_N.split(stuff);
				
				for (int i=1;i<bits.length;i++){
					
					File f = new File( bits[i] );

					if ( f.isFile()){
					
						dropFile( f );
					}
				}
			}else{
				
				File f = new File( stuff );

				if ( f.exists()){
					
					dropFile( f );
					
				}else{
					String lc_stuff = stuff.toLowerCase( Locale.US );
					
					if ( 	lc_stuff.startsWith( "http:" ) || 
							lc_stuff.startsWith( "https:" ) ||
							lc_stuff.startsWith( "magnet: ")){
				
						dropURL( stuff );
						
					}else{
						
						Debug.out( "Failed to handle drop for '" + stuff + "'" );
					}
				}
			}
		}else if ( payload instanceof URLTransfer.URLType ){
			
			String url = ((URLTransfer.URLType)payload).linkURL;
			
			if ( url != null ){
				
				dropURL( url );
				
			}else{
				
				Debug.out( "Failed to handle drop for '" + payload + "'" );
			}
		}
	}
	
	private void
	dropURL(
		String	str )
	{
		input_area.setText( input_area.getText() + str );
	}
	
	private void
	dropFile(
		final File	file )
	{
		try{			
			if ( file.exists() && file.canRead()){
				
				new AEThread2( "share async" )
				{
					public void
					run()
					{
						PluginInterface pi = plugin.getPluginInterface();
						
						Map<String,String>	properties = new HashMap<String, String>();
						
						String[]	networks;
						
						if ( chat.isAnonymous()){
							
							networks = AENetworkClassifier.AT_NON_PUBLIC;
							
						}else{
							
							networks = AENetworkClassifier.AT_NETWORKS;
						}
						
						String networks_str = "";
						
						for ( String net: networks ){
							
							networks_str += (networks_str.length()==0?"":",") + net;
						}
						
						properties.put( ShareManager.PR_PERSONAL, "true" );
						properties.put( ShareManager.PR_NETWORKS, networks_str );
						properties.put( ShareManager.PR_USER_DATA, "buddyplugin:share" );
						
						Torrent 	torrent;

						try{
							if ( file.isFile()){
								
								ShareResourceFile srf = pi.getShareManager().addFile( file, properties );
								
								torrent = srf.getItem().getTorrent();
								
							}else{
								
								ShareResourceDir srd = pi.getShareManager().addDir( file, properties );
								
								torrent = srd.getItem().getTorrent();
							}	
													
							final Download download = pi.getPluginManager().getDefaultPluginInterface().getShortCuts().getDownload( torrent.getHash());
	
							if ( download == null ){
								
								throw( new Exception( "Download no longer exists" ));
								
							}
							Utils.execSWTThread(
								new Runnable() {
									
									public void 
									run()
									{
										dropDownload( download );

									}
								});
							
						}catch( Throwable e ){
							
							dropFailed( file.getName(), e );
						}
					}
				}.start();
	
			}else{
				
				throw( new Exception( "File '" + file + "' does not exist or is not accessible" ));
			}
			
		}catch( Throwable e ){
			
			dropFailed( file.getName(), e );
		}
	}
	
	private void
	dropDownload(
		Download		download )
	{		
		String magnet = UrlUtils.getMagnetURI( download, 80 );
		
		InetSocketAddress address = chat.getMyAddress();
		
		if ( address != null ){
			
			String address_str = AddressUtils.getHostAddress(address) + ":" + address.getPort();
			
			String arg = "&xsource=" + UrlUtils.encode( address_str );
			
			if ( magnet.length() + arg.length() < MAX_MSG_LENGTH ){
				
				magnet += arg;
			}
		}
		
		if ( magnet.length() < MAX_MSG_LENGTH-10 ){
					
			magnet += "[[$dn]]";
		}
		
		plugin.getBeta().tagDownload( download );
		
		download.setForceStart( true );
		
		input_area.setText( input_area.getText() + magnet );
	}
	
	private void
	dropDownloadFile(
		DiskManagerFileInfo		file )
	{
		try{
			Download download = file.getDownload();
			
			if ( download.getTorrent().isSimpleTorrent()){
				
				dropDownload( download );
				
				return;
			}
			
			File target = file.getFile( true );
			
			if (	 target.exists() && 
					( file.getDownloaded() == file.getLength() ||
					( download.isComplete() && !file.isSkipped()))){	// just in case cached file completion is borked
				
				dropFile( target );
				
			}else{
				
				throw( new Exception( "File is incomplete or missing" ));
			}
		}catch( Throwable e ){
			
			dropFailed( file.getFile(true).getName(), e );
		}
	}
	
	private void
	dropFailed(
		String		content,
		Throwable 	e )
	{
		UIManager ui_manager = plugin.getPluginInterface().getUIManager();
				
		String details = 
			MessageText.getString( 
				"azbuddy.dchat.share.fail.msg",
				new String[]{ content, Debug.getNestedExceptionMessage( e ) });
			
		ui_manager.showMessageBox(
				"azbuddy.dchat.share.fail.title",
				"!" + details + "!",
				UIManagerEvent.MT_OK );
	}
	
	protected void
	close()
	{
		if ( shell != null ){
		
			shell.dispose();
		}
	}
	
	protected void
	closed()
	{
		chat.removeListener( this );
		
		chat.destroy();
		
		view.unregisterUI( chat );
	}
	
	public void 
	stateChanged(
		final boolean avail ) 
	{
		if ( buddy_table.isDisposed()){
			
			return;
		}
	
		Utils.execSWTThread(
			new Runnable()
			{
				public void
				run()
				{
					if ( buddy_table.isDisposed()){
						
						return;
					}
					
					input_area.setEnabled( avail );
					
						// update as key may now be available
					
					nickname.setMessage( chat.getDefaultNickname());
				}
			});
	}
	
	public void 
	updated() 
	{
		if ( status.isDisposed()){
			
			return;
		}
	
		Utils.execSWTThread(
			new Runnable()
			{
				public void
				run()
				{
					if ( status.isDisposed()){
						
						return;
					}
				
					status.setText( chat.getStatus());
					
					boolean	is_shared = chat.isSharedNickname();
					
					if ( is_shared != shared_nick_button.getSelection()){
						
						shared_nick_button.setSelection( is_shared );
					}
						
					if ( !nickname.isFocusControl()){
						
						String old_nick = nickname.getText().trim();
							
						String new_nick = chat.getNickname( false );
						
						if ( !new_nick.equals( old_nick )){
								
							nickname.setText( new_nick );
						}
					}
					
					if ( table_resort_required ){
						
						updateTable( false );
					}
					
					updateTableHeader();
				}
			});
	}
	
	private void
	sortParticipants()
	{
		synchronized( participants ){
			Collections.sort(
				participants,
				new Comparator<ChatParticipant>()
				{
					private Comparator<String> comp = new FormattersImpl().getAlphanumericComparator( true );
					
					public int 
					compare(
						ChatParticipant p1, 
						ChatParticipant p2 ) 
					{
						boolean	b_p1 = p1.hasNickname();
						boolean	b_p2 = p2.hasNickname();
						
						if ( b_p1 == b_p2 ){
						
							return( comp.compare( p1.getName( ftux_ok ), p2.getName( ftux_ok )));
							
						}else if ( b_p1 ){
							
							return( -1 );
							
						}else{
							
							return( 1 );
						}
					}
				});
		}
	}
	
	public void
	participantAdded(
		ChatParticipant		participant )
	{
		synchronized( participants ){
			
			participants.add( participant );
			
			table_resort_required = true;
		}
		
		updateTable( true );
	}
	
	public void
	participantChanged(
		final ChatParticipant		participant )
	{
		if ( !buddy_table.isDisposed()){

			Utils.execSWTThread(
				new Runnable()
				{
					public void
					run()
					{
						if ( buddy_table.isDisposed()){

							return;
						}
						
						TableItem[] items = buddy_table.getItems();
						
						String	name = participant.getName( ftux_ok );
						
						for ( TableItem item: items ){
							
							if ( item.getData() == participant ){
								
								setProperties( item, participant );
								
								String old_name = item.getText(0);
								
								if ( !old_name.equals( name )){
								
									item.setText( 0, name );
									
									table_resort_required = true;
								}
							}
						}
					}
				});
		}			
	}
	
	public void
	participantRemoved(
		ChatParticipant		participant )
	{
		synchronized( participants ){
			
			participants.remove( participant );
			
			participant_last_message_map.remove( participant );
		}
		
		updateTable( true );
	}
	
	protected void
	sendMessage(
		String		text )
	{
		//logChatMessage( plugin.getNickname(), Colors.green, text );
		
		try{
				// decode escaped unicode chars
			
			Pattern p = Pattern.compile("(?i)\\\\u([\\dabcdef]{4})");

			Matcher m = p.matcher( text );

			boolean result = m.find();

			if ( result ){

				StringBuffer sb = new StringBuffer();

		    	while( result ){
		    		
		    		 String str = m.group(1);
		    		 
		    		 int unicode = Integer.parseInt( str, 16 );
		    		 
		    		 m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf((char)unicode)));
		    		 
		    		 result = m.find(); 
		    	 }

				m.appendTail(sb);

				text = sb.toString();
			}
		}catch( Throwable e ){
		}
		
		chat.sendMessage( text, new HashMap<String, Object>());
	}
	
	private static String
	expand(
		Map<String,String>	params,
		String				str,
		boolean				url_decode )
	{
		int	pos = 0;
		
		String result = "";
		
		while( true ){
			
			int new_pos = str.indexOf('$', pos );
			
			if ( new_pos == -1 ){
				
				result += str.substring( pos );
				
				break;
			}
			
			result += str.substring( pos, new_pos );
			
			int end_pos = str.length();
			
			for ( int i=new_pos+1;i<end_pos;i++){
				
				char c = str.charAt( i );
				
				if ( !( Character.isLetterOrDigit(c) || c == '_' )){
					
					end_pos = i;
					
					break;
				}
			}
			
			String param = str.substring( new_pos+1, end_pos );
			
			String value = params.get( param );
			
			if ( value == null ){
		
				pos = new_pos + 1;
				
				result += "$";
				
			}else{
				
				if ( url_decode ){
					
					result += UrlUtils.decode( value );
					
				}else{
					
					result += value;
				}
				
				pos = end_pos;
			}
		}
		
		return( result );
	}
	
	public void
	messageReceived(
		final ChatMessage	message,
		boolean				sort_outstanding )
	{
		if ( sort_outstanding ){
		
				// we'll pick things up on the 'messagesChanged' callback after the sort
			
			return;
		}
		
		if ( !log.isDisposed()){

			Utils.execSWTThread(
				new Runnable()
				{
					public void
					run()
					{
						if ( log.isDisposed()){

							return;
						}
													
						logChatMessage( message );
					}
				});
		}
	}
	
	public void 
	messagesChanged() 
	{
		if ( !log.isDisposed()){

			Utils.execSWTThread(
				new Runnable()
				{
					public void
					run()
					{
						if ( log.isDisposed()){

							return;
						}
						
						try{								
							resetChatMessages();
							
							BuddyPluginBeta.ChatMessage[] history = chat.getHistory();
															
							logChatMessages( history );
							
						}catch( Throwable e ){
							
							Debug.printStackTrace(e);
						}
					}
				});
		}
	}
	
	private String	previous_says 				= null;
	private int		previous_says_mt			= -1;
	private long	last_seen_message			= -1;	
	private long	last_seen_message_pending	= -1;
	
	private void
	resetChatMessages()
	{
		log.setText( "" );
		
		messages.clear();
		
		previous_says	= null;
		
		synchronized( participants ){
			
			participant_last_message_map.clear();
		}
	}
	
	private void
	logChatMessage(
		ChatMessage		message )
	{
		logChatMessages( new ChatMessage[]{ message } );
	}
	
	private final SimpleDateFormat time_format1 	= new SimpleDateFormat( "HH:mm" );
	private final SimpleDateFormat time_format2a 	= new SimpleDateFormat( "EE h" );
	private final SimpleDateFormat time_format2b 	= new SimpleDateFormat( "a" );
	private final SimpleDateFormat time_format3 	= new SimpleDateFormat( "dd/MM" );

	private String
	getChatTimestamp(
		long		now,
		long		time )
	{
		long 	age 	= now - time;
		Date	date	= new Date( time );
		
		if ( age < 24*60*60*1000L ){
			
			return( time_format1.format( date ));
			
		}else if ( age < 7*24*60*60*1000L ){
			
			return( time_format2a.format( date ) + time_format2b.format( date ).toLowerCase());

		}else{
			
			return( time_format3.format( date ));
		}
	}
	
	private void
	logChatMessages(
		ChatMessage[]		all_messages )
	{	
		long	now = SystemTime.getCurrentTime();
		
		final int	initial_log_length = log.getText().length();
		
		StringBuilder appended = new StringBuilder( 2048 );
		
		List<StyleRange>	new_ranges = new ArrayList<StyleRange>();
		
		ChatMessage	last_message_not_ours 		= null;
		
		boolean	ignore_ratings 		= beta.getHideRatings();
		boolean	ignore_search_subs 	= beta.getHideSearchSubs();
		
		for ( ChatMessage message: all_messages ){
			
			if ( messages.containsKey( message )){
				
				continue;
			}
						
			String original_msg		= message.getMessage();

			if ( !message.isIgnored() && original_msg.length() > 0 ){
				
				if ( ignore_ratings || ignore_search_subs ){
					
					int origin = message.getFlagOrigin();
					
					if ( origin == BuddyPluginBeta.FLAGS_MSG_ORIGIN_RATINGS && ignore_ratings ){
						
						continue;
						
					}else if ( origin == BuddyPluginBeta.FLAGS_MSG_ORIGIN_SUBS && ignore_search_subs ){
						
						continue;
					}
				}
				
				long time = message.getTimeStamp();

				final ChatParticipant participant = message.getParticipant();

				final boolean	is_me = participant.isMe();
				
				if ( !is_me ){
					
					last_message_not_ours		= message;
				}
				
				final int message_start_appended_length 	= appended.length();
				final int message_start_style_index			= new_ranges.size();
				
				String	nick 	= message.getNickName();
				
				int	message_type = message.getMessageType();
								
				Font 	default_font 	= null;
				Color	default_colour	= null;

				Font 	info_font 	= null;
				Color	info_colour	= Colors.grey;

				Color colour = Colors.blues[Colors.FADED_DARKEST];
				
				if ( message_type ==  ChatMessage.MT_INFO ){
					
					if ( original_msg.startsWith( "*" ) && original_msg.endsWith( "*" )){
						
						original_msg = original_msg.substring( 1, original_msg.length()-1 );
						
						info_colour = Colors.black;
						info_font	= bold_font;
						
					}else{
						
						colour = Colors.grey;
					}
				}else if ( message_type ==  ChatMessage.MT_ERROR ){
						
					colour = Colors.red;		
					
				}else if ( participant.isPinned() || is_me ){
					
					colour = Colors.fadedGreen;
					
				}else if ( message.isNickClash()){
					
					colour = Colors.red;
				}
								
				String stamp = getChatTimestamp( now, time );
				
				ChatMessage	last_message;
				
				synchronized( participants ){
					
					last_message = participant_last_message_map.get( participant );
					
					participant_last_message_map.put( participant, message );
				}

				boolean is_me_msg = message.getFlagType() == BuddyPluginBeta.FLAGS_MSG_TYPE_ME;
				
				String 	says;
				int		stamp_len;
				
				int	was_len = 0;
				
				if ( message_type != ChatMessage.MT_NORMAL ){
					
					says = "[" + stamp + "]";
					
					stamp_len = says.length();
					
				}else{
					
					says = "[" + stamp + "] " + (nick.length()>20?(nick.substring(0,16) + "..."):nick);
			
					stamp_len = stamp.length() + 3;
					
					if ( last_message != null && !is_me ){
						
						String last_nick = last_message.getNickName();
						
						if ( !nick.equals(last_nick)){
							
							String was = " (was " + (last_nick.length()>20?(last_nick.substring(0,16) + "..."):last_nick) + ")";
							
							says += was;
							
							was_len = was.length();
						}
					}
				}
										
				if ( message_type == ChatMessage.MT_NORMAL ){
					
					if ( is_me_msg ){
					
						says += " ";
						
						default_colour	= colour;
						
						if ( is_me ){
							
							default_font = italic_font;
						}
						
					}else{
						
						says += "\n";
					}
				}else{
					
					says += " ";
					
					if ( message_type ==  ChatMessage.MT_ERROR ){
						
						default_colour = colour;
					}
				}
				

				if ( 	previous_says == null || 
						previous_says_mt != message_type || 
						is_me_msg || 
						!previous_says.equals( says )){
					
					previous_says 		= says;
					previous_says_mt	= message_type;
					
					int	start = initial_log_length + appended.length();
							
					appended.append( says );
					
					{
						StyleRange styleRange = new MyStyleRange(message);
						styleRange.start = start;
						styleRange.length = stamp_len;
						styleRange.foreground = Colors.grey;
						
						if ( is_me ){
							
							styleRange.font = italic_font;
						}
						
						new_ranges.add( styleRange);
					}
					
					if ( colour != Colors.black ){
						
						int rem = says.length() - stamp_len;
						
						if ( rem > 0 ){
							
							StyleRange styleRange = new MyStyleRange(message);
							styleRange.start = start + stamp_len;
							styleRange.length = rem - was_len;
							styleRange.foreground = colour;
							styleRange.data = participant;
							
							if ( is_me ){
								
								styleRange.font = italic_font;
							}
							
							new_ranges.add( styleRange);
						}
					}
				}
				
				final int start = initial_log_length + appended.length();
								
				String rendered_msg = renderMessage( beta, chat, message, original_msg, message_type, start, new_ranges, info_font, info_colour, bold_font );
								
				appended.append( rendered_msg ); 

					// apply any default styles

				if ( default_font != null || default_colour != null ){
					
					final int message_start_log_length 	= initial_log_length + message_start_appended_length;
						
					int	pos = message_start_log_length;
					
					for ( int i=message_start_style_index;i<new_ranges.size();i++){
						
						StyleRange style = new_ranges.get(i);
						
						int style_start 	= style.start;
						int style_length	= style.length;
						
						if ( style_start > pos ){
							
							//System.out.println( "    " + pos + "-" + (style_start-1 ));
							
							StyleRange styleRange 	= new MyStyleRange(message);
							styleRange.start 		= pos;
							styleRange.length 		= style_start - pos;
							
							if ( default_colour != null ){
								styleRange.foreground 	= default_colour;
							}
							if ( default_font != null ){
								styleRange.font = default_font;
							}
							
							new_ranges.add( i, styleRange);
							
							i++;
						}
						
						pos = style_start + style_length;
					}
									
					int message_end_log_length	= initial_log_length + appended.length();
					
					if ( pos < message_end_log_length ){
						
						//System.out.println( "    " + pos + "-" + (message_end_log_length-1) );
	
						StyleRange styleRange 	= new MyStyleRange(message);
						styleRange.start 		= pos;
						styleRange.length 		= message_end_log_length - pos;
						
						if ( default_colour != null ){
							styleRange.foreground 	= default_colour;
						}
						if ( default_font != null ){
							styleRange.font = default_font;
						}
						
						new_ranges.add( styleRange);
					}
				}
				
				appended.append( "\n" ); 
				
				int	actual_length = appended.length() - message_start_appended_length;
				
				messages.put( message, actual_length );
			}
		}

		if ( appended.length() > 0 ){
		
			try{
				log.setVisible( false );
				
				log.append( appended.toString());
				
				if ( new_ranges.size() > 0 ){
				
					List<StyleRange> existing_ranges = Arrays.asList( log.getStyleRanges());
					
					List<StyleRange> all_ranges = new ArrayList<StyleRange>( existing_ranges.size() + new_ranges.size());
					
					all_ranges.addAll( existing_ranges );
					
					all_ranges.addAll( new_ranges );
					
					StyleRange[] ranges = all_ranges.toArray( new StyleRange[ all_ranges.size()]);
					
					for ( StyleRange sr: ranges ){
						
						sr.borderStyle = SWT.NONE;
					}
					
					log.setStyleRanges( ranges );
					
					log_styles = ranges;				
				}
				
				
				Iterator<Integer> it = null;
				
				int max_lines 	= beta.getMaxUILines();
				int max_chars	= beta.getMaxUICharsKB() * 1024;
				
				while ( messages.size() > max_lines || log.getText().length() > max_chars ){
					
					if ( it == null ){
						
						it = messages.values().iterator();
					}
					
					if ( !it.hasNext()){
						
						break;
					}
					
					int to_remove = it.next();
					
					it.remove();
					
					log.replaceTextRange( 0,  to_remove, "" );
					
					log_styles = log.getStyleRanges();
				}
				
				log.setSelection( log.getText().length());
				
			}finally{
			
				log.setVisible( true );
			}
			
			if ( last_message_not_ours != null ){
				
				long last_message_not_ours_time = last_message_not_ours.getTimeStamp();
				
				boolean	mesages_seen = true;
				
				if ( build_complete ){
				
					if ( 	( !log.isVisible()) || 
							( shell != null && shell.getMinimized()) ||
							log.getDisplay().getFocusControl() == null ){
							
						if ( last_message_not_ours_time > last_seen_message ){
						
							last_seen_message_pending = last_message_not_ours_time;
							
							view.betaMessagePending( chat, log, last_message_not_ours );
							
							mesages_seen = false;
						}
					}else{
						
						last_seen_message = last_message_not_ours_time;
					}
				}else{
					
						// assume that during construction the messages will be seen
					
					if ( last_message_not_ours_time > last_seen_message ){
					
						last_seen_message = last_message_not_ours_time;
					}
				}
				
				if ( mesages_seen ){
					
					for ( ChatMessage msg: all_messages ){
						
						msg.setSeen( true );
					}
				}
			}
		}
	}
	
	protected static String
	renderMessage(
		BuddyPluginBeta		beta,
		ChatInstance		chat,
		ChatMessage			message,
		String				original_msg,
		int					message_type,
		int					start,
		List<StyleRange>	new_ranges,
		Font				info_font,
		Color				info_colour,
		Font				bold_font )
	{
		String msg = original_msg;
		
		try{
			{
				List<Object>		segments = new ArrayList<Object>();
				
				int	pos = 0;
				
				while( true ){
					
					int old_pos = pos;
					
					pos = original_msg.indexOf( ':', old_pos );
					
					if ( pos == -1 ){
						
						String tail = original_msg.substring( old_pos );
						
						if ( tail.length() > 0 ){
							
							segments.add( tail );
						}
						
						break;
					}
					
					boolean	was_url = false;
					
					String	protocol = "";
					
					for (int i=pos-1; i>=0; i-- ){
						
						char c = original_msg.charAt(i);
						
						if ( !Character.isLetterOrDigit( c )){
							
							if ( c == '"' ){
								
								protocol = c + protocol;
							}
							
							break;
						}
						
						protocol = c + protocol;
					}
					
					if ( protocol.length() > 0 ){
						
						char term_char = ' ';
						
						if ( protocol.startsWith( "\"" )){
							
							term_char = '"';
						}
						
						int	url_start 	= pos - protocol.length();
						int	url_end 	= original_msg.length();
						
						for ( int i=pos+1;i<url_end;i++){
							
							char c = original_msg.charAt( i );
							
							if ( c == term_char || ( term_char == ' ' && Character.isWhitespace( c ))){
								
								url_end = term_char==' '?i:(i+1);
								
								break;
							}
						}
							
						if ( url_end > pos+1 && !Character.isDigit( protocol.charAt(0))){
							
							try{									
								String url_str = protocol + original_msg.substring( pos, url_end );
						
								if ( url_str.startsWith( "\"" ) && url_str.endsWith( "\"" )){
									
									url_str = url_str.substring( 1, url_str.length()-1 );
									
									protocol = protocol.substring(1);
								}
								
								URL	url = new URL( url_str );
								
								if ( url_start > old_pos ){
									
									segments.add( original_msg.substring( old_pos, url_start ));
								}
								
								segments.add( url );
								
								was_url = true;
								
								pos	= url_end;
								
							}catch( Throwable e ){
								
							}
						}
					}
					
					if ( !was_url ){
						
						pos++;
						
						segments.add( original_msg.substring( old_pos, pos ) );
					}
				}
				
				if ( segments.size() > 1 ){
					
					List<Object>	temp = new ArrayList<Object>( segments.size());
				
					String	str = "";
					
					for ( Object obj: segments ){
						
						if ( obj instanceof String ){
					
							str += obj;
							
						}else{
							
							if ( str.length() > 0 ){
								
								temp.add( str );
							}
							
							str = "";
							
							temp.add( obj );
						}
					}
				
					if ( str.length() > 0 ){
						
						temp.add( str );
					}
					
					segments = temp;
				}
									
				Map<String,String>	params = new HashMap<String,String>();
				
				for ( int i=0;i<segments.size(); i++ ){
					
					Object obj = segments.get(i);
				
					if ( obj instanceof URL ){
						
						params.clear();
						
						String str = ((URL)obj).toExternalForm();
						
						int	qpos = str.indexOf( '?' );
						
						if ( qpos > 0 ){
							
							int	hpos = str.lastIndexOf( "[[" );

							String[]	bits = str.substring( qpos+1, hpos==-1?str.length():hpos ).split( "&" );
							
							for ( String bit: bits ){
								
								String[] temp = bit.split( "=", 2 );
								
								if ( temp.length == 2 ){
									
									params.put( temp[0], temp[1] );
								}
							}
														
							if ( hpos > 0 && str.endsWith( "]]" )){
								
								str = 	str.substring( 0, hpos ) + 
										"[[" +
										expand( params, str.substring( hpos+2, str.length()-2 ), false ) +
										"]]";
								
								try{
									segments.set( i, new URL( str ));
									
								}catch( Throwable e ){
									
									Debug.out( e );
								}
							}
						}else{
							
							int	hpos = str.lastIndexOf( "[[" );
																						
							if ( hpos > 0 && str.endsWith( "]]" )){
								
								str = 	str.substring( 0, hpos ) + 
										"[[" +
										str.substring( hpos+2, str.length()-2 ) +
										"]]";
								
								try{
									segments.set( i, new URL( str ));
									
								}catch( Throwable e ){
									
									Debug.out( e );
								}
							}
						}
					}else{
						
						String str = (String)obj;
						
						if ( params.size() > 0 ){
							
							segments.set( i, expand( params, str, true ));
						}
					}
				}
					
				StringBuilder sb = new StringBuilder( 1024 );
				
				for ( Object obj: segments ){
					
					if ( obj instanceof URL ){
					
						sb.append("\"").append(((URL) obj).toExternalForm()).append("\"");
						
					}else{
						
						String segment_str = (String)obj;
						
						try{
							String my_nick = chat.getNickname( true );
							
							if ( 	my_nick.length() > 0 && 
									segment_str.contains( my_nick ) &&
									message_type ==  ChatMessage.MT_NORMAL ){
								
								StringBuilder temp = new StringBuilder( segment_str.length() + 1024 );
								
								int	nick_len = my_nick.length();
																	
								int	segment_len = segment_str.length();
								
								int	segment_pos = 0;
																	
								while( segment_pos < segment_len ){
									
									int next_pos = segment_str.indexOf( my_nick, segment_pos );
									
									if ( next_pos >= 0 ){
										
										temp.append( segment_str.substring( segment_pos, next_pos ));
										
										boolean	match = true;
										
										if ( next_pos > 0 ){
											
											if ( Character.isLetterOrDigit( segment_str.charAt( next_pos-1 ))){
												
												match = false;
											}
										}
										
										int nick_end = next_pos + nick_len;
										
										if ( nick_end < segment_len ){
											
											if ( Character.isLetterOrDigit( segment_str.charAt(nick_end ))){
												
												match = false;
											}
										}
										
										if ( match ){
										
											temp.append("\"chat:nick[[").append(UrlUtils.encode(my_nick)).append("]]\"");

										}else{
											
											temp.append( my_nick );
										}
										
										segment_pos = next_pos + nick_len;
									
									}else{
										
										temp.append( segment_str.substring(segment_pos));
										
										break;
									}
								}
								
								segment_str = temp.toString();
							}
						}catch( Throwable e ){
						
							Debug.out( e );
						}
						
						sb.append( segment_str );
					}
				}
				
				msg = sb.toString();
			}
			
			{	
					// should rewrite this one day to use the segments above directly... We'd need to handle URLs in expansions though
				
				int	next_style_start = start;
				
				int	pos = 0;
				
				while( pos < msg.length()){
					
					pos = msg.indexOf( ':', pos );
					
					if ( pos == -1 ){
						
						break;
					}
					
					String	protocol = "";
					
					for (int i=pos-1; i>=0; i-- ){
						
						char c = msg.charAt(i);
						
						if ( !Character.isLetterOrDigit( c )){
							
							if ( c == '"' ){
								
								protocol = c + protocol;
							}
							
							break;
						}
						
						protocol = c + protocol;
					}
					
					if ( protocol.length() > 0 ){
						
						char term_char = ' ';
						
						if ( protocol.startsWith( "\"" )){
							
							term_char = '"';
						}
						
						int	url_start 	= pos - protocol.length();
						int	url_end 	= msg.length();
						
						for ( int i=pos+1;i<url_end;i++){
							
							char c = msg.charAt( i );
							
							if ( c == term_char || ( term_char == ' ' && Character.isWhitespace( c ))){
								
								url_end = term_char==' '?i:(i+1);
								
								break;
							}
						}
							
						if ( url_end > pos+1 && !Character.isDigit( protocol.charAt(0))){
							
							try{									
								String url_str = protocol + msg.substring( pos, url_end );
						
								if ( url_str.startsWith( "\"" ) && url_str.endsWith( "\"" )){
									
									url_str = url_str.substring( 1, url_str.length()-1 );
									
									protocol = protocol.substring(1);
								}

								if ( protocol.equalsIgnoreCase( "chat" )){
									
									if ( url_str.toLowerCase( Locale.US ).startsWith( "chat:anon" )){
										
										if ( beta != null && !beta.isI2PAvailable()){
											
											throw( new Exception( "Anonymous chat unavailable" ));
										}
									}
								}else{
								
										// test that it is a valid URL
									
									URL	url = new URL( url_str );
								}
								
								String original_url_str = url_str;
								
								String display_url = UrlUtils.decode( url_str );

									// support a lame way of naming links - just append [[<url-encoded desc>]] to the URL
																		
								int hack_pos = url_str.lastIndexOf( "[[" );
								
								if ( hack_pos > 0 && url_str.endsWith( "]]" )){
									
									String substitution = url_str.substring( hack_pos + 2, url_str.length() - 2  ).trim();
									
									url_str = url_str.substring( 0, hack_pos );
									
										// prevent anything that looks like a URL from being used as the display
										// text to avoid 'confusion' but be lenient for safe protocols

									boolean safe = protocol.equals( "azplug" ) || protocol.equals( "chat" );
										
									if ( safe || UrlUtils.parseTextForURL( substitution, true ) == null ){
										
										display_url = UrlUtils.decode( substitution );
										
									}else{
										
										display_url = UrlUtils.decode( url_str );
									}										
								}
								
								if ( term_char != ' ' || !display_url.equals( original_url_str )){
									
									int	old_len = msg.length();
									
									msg = msg.substring( 0, url_start ) + display_url + msg.substring( url_end );
									
										// msg has probably changed length, update the end-pointer accordingly
									
									url_end += (msg.length() - old_len );
								}
								
								int	this_style_start 	= start + url_start;
								int this_style_length	= display_url.length();
								
								if ( this_style_start > next_style_start ){
									
									if ( message_type ==  ChatMessage.MT_INFO ){
										
										StyleRange styleRange 	= new MyStyleRange(message);
										styleRange.start 		= next_style_start;
										styleRange.length 		= this_style_start - next_style_start;
										styleRange.foreground 	= info_colour;
										styleRange.font			= info_font;
									
										new_ranges.add( styleRange);
										
										next_style_start = this_style_start + this_style_length;
									}
								}
								
									/* Check that the URL is actually going tobe useful. IN particular, if it is a magnet URI with
									 * no hash and no &fl links then it ain't gonna work
									 */
								
								boolean	will_work = true;
								
								try{
									
									String lc_url = url_str.toLowerCase( Locale.US );
									
									if ( lc_url.startsWith( "magnet" )){
										
										if ( 	( !lc_url.contains( "btih:" )) ||
												lc_url.contains( "btih:&" ) ||
												lc_url.endsWith( "btih:" )){
											
												// no hash
											
											if ( !lc_url.contains( "&fl=" )){
												
													// no direct link
													
												will_work = false;
											}
										}
									}else if ( lc_url.startsWith( "chat:nick" )){
										
										will_work = false;
									}
								}catch( Throwable e ){
									
								}
								
								if ( will_work ){
									
									StyleRange styleRange 	= new MyStyleRange( message );
									styleRange.start 		= this_style_start;
									styleRange.length 		= this_style_length;
									styleRange.foreground 	= Colors.blue;
									styleRange.underline 	= true;
									
										// DON'T store the URL object because in their wisdom SWT invokes the .equals method
										// on data objects when trying to find 'similar' ones, and for URLs this causes
										// a name service lookup...
									
									styleRange.data = url_str;
									
									new_ranges.add( styleRange);
									
								}else{
									
									StyleRange styleRange 	= new MyStyleRange( message );
									styleRange.start 		= this_style_start;
									styleRange.length 		= this_style_length;
									styleRange.font 		= bold_font;
									
									new_ranges.add( styleRange);
								}
							}catch( Throwable e ){
								
								//e.printStackTrace();
							}
						}
						
						pos = url_end;

					}else{
						
						pos = pos+1;
					}		
				}
			
				if ( next_style_start < start + msg.length() ){
					
					if ( message_type ==  ChatMessage.MT_INFO ){
						
						StyleRange styleRange 	= new MyStyleRange( message );
						styleRange.start 		= next_style_start;
						styleRange.length 		= start + msg.length() - next_style_start;
						styleRange.foreground 	= info_colour;
						styleRange.font			= info_font;
						
						new_ranges.add( styleRange);
					}
				}
			}
		}catch( Throwable e ){
			
			Debug.out( e );
		}
		
		return( msg );
	}
	
	public void
	activate()
	{
		if ( last_seen_message_pending > last_seen_message ){
			
			last_seen_message = last_seen_message_pending;
		}
		
		view.betaMessagePending( chat, log, null );
		
		List<ChatMessage>	unseen = chat.getUnseenMessages();
		
		for ( ChatMessage msg: unseen ){
			
			msg.setSeen( true );
		}
	}
	
	private static class
	MyStyleRange
		extends StyleRange
	{
		private ChatMessage	message;
		
		MyStyleRange(
			ChatMessage		_msg )
		{
			message = _msg;
			
			data = message;
		}
		
		MyStyleRange(
			MyStyleRange	other )
		{	
			super( other );
			
			message = other.message;
		}
	}
}
