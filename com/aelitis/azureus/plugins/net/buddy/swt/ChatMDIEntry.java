/*
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

package com.aelitis.azureus.plugins.net.buddy.swt;


import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatAdapter;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatInstance;
import com.aelitis.azureus.ui.UIFunctionsManager;
import com.aelitis.azureus.ui.common.viewtitleinfo.ViewTitleInfo;
import com.aelitis.azureus.ui.common.viewtitleinfo.ViewTitleInfoManager;
import com.aelitis.azureus.ui.mdi.MdiCloseListener;
import com.aelitis.azureus.ui.mdi.MdiEntry;
import com.aelitis.azureus.ui.mdi.MdiEntryDropListener;
import com.aelitis.azureus.ui.mdi.MultipleDocumentInterface;


public class ChatMDIEntry implements ViewTitleInfo
{
	private final MdiEntry mdi_entry;
	
	private final ChatInstance chat;
	
	private ChatView		view;
	private String			drop_outstanding;
	
	private final ChatAdapter adapter = 
		new ChatAdapter()
		{
			@Override
			public void 
			updated() 
			{
				update();
			}
		};
		
	public 
	ChatMDIEntry(
		ChatInstance 	_chat, 
		MdiEntry 		_entry) 
	{
		chat		= _chat;
		
		mdi_entry 	= _entry;
			
		setupMdiEntry();
	}
	
	private void 
	setupMdiEntry() 
	{
		mdi_entry.setViewTitleInfo( this );
		
		MdiEntryDropListener drop_listener = 
			new MdiEntryDropListener() 
			{
				public boolean 
				mdiEntryDrop(
					MdiEntry 	entry, 
					Object		payload ) 
				{
					if ( payload instanceof String[] ){
						
						String[] derp = (String[])payload;
						
						if ( derp.length > 0 ){
							
							payload = derp[0];
						}
					}
					
					if (!(payload instanceof String)){
						
						return false;
					}
					
					MultipleDocumentInterface mdi = UIFunctionsManager.getUIFunctions().getMDI();

					if ( mdi != null ){
						
						String drop = (String)payload;
						
						if ( view == null ){
							
							drop_outstanding = drop;
							
						}else{
							
							view.handleDrop( drop );
						}
						
						mdi.showEntry( mdi_entry );
						
						return( true );
						
					}else{
					
						return( false );
					}
				}
			};

		mdi_entry.addListener( drop_listener );
		
		mdi_entry.addListener(
			new MdiCloseListener()
			{
				public void 
				mdiEntryClosed(
					MdiEntry 	entry,
					boolean 	user) 
				{
					chat.destroy();
				}
			});
		
		chat.addListener( adapter );
	}

	protected void
	setView(
		ChatView		_view )
	{
		view = _view;
		
		String drop = drop_outstanding;
		
		if ( drop != null ){
			
			drop_outstanding = null;
		
			view.handleDrop( drop );
		}
	}
	
	private void
	update()
	{
		mdi_entry.redraw();
	
		ViewTitleInfoManager.refreshTitleInfo( mdi_entry.getViewTitleInfo());
	}
	
	public Object 
	getTitleInfoProperty(
		int propertyID ) 
	{
		switch( propertyID ){
		
			case ViewTitleInfo.TITLE_INDICATOR_TEXT_TOOLTIP:{
				
				return( chat.getName());
			}
			case ViewTitleInfo.TITLE_TEXT:{
				
				return( chat.getName( true ));
			}
			case ViewTitleInfo.TITLE_INDICATOR_COLOR:{
							
				if ( chat.getMessageOutstanding()){
					
					if ( chat.hasUnseenMessageWithNick()){
							
						return( SBC_ChatOverview.COLOR_MESSAGE_WITH_NICK );
					}
				}
				
				return( null );
			}
			case ViewTitleInfo.TITLE_INDICATOR_TEXT:{
				
				if ( chat.getMessageOutstanding()){
					
					return( "*" );
					
				}else{
					
					return( null );
				}
						
			}
		}
		
		return( null );
	}
}
