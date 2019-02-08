/**
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package com.aelitis.azureus.ui.swt.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.TransferSpeedValidator;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;
import org.gudy.azureus2.core3.util.TimerEventPeriodic;
import org.gudy.azureus2.ui.swt.Messages;
import org.gudy.azureus2.ui.swt.Utils;
import org.gudy.azureus2.ui.swt.components.BufferedLabel;
import org.gudy.azureus2.ui.swt.config.IntParameter;
import org.gudy.azureus2.ui.swt.config.Parameter;
import org.gudy.azureus2.ui.swt.config.ParameterChangeAdapter;
import org.gudy.azureus2.ui.swt.plugins.UISWTView;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.pluginsimpl.UISWTViewCoreEventListener;
import org.gudy.azureus2.ui.swt.views.configsections.ConfigSectionStartShutdown;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreLifecycleAdapter;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.aelitis.azureus.core.AzureusCoreFactory;

/**
 * @author TuxPaper
 * @created Apr 7, 2007
 *
 */
public class ViewQuickConfig
	implements UISWTViewCoreEventListener
{
	private UISWTView swtView;
	
	Composite	composite;
	
	public ViewQuickConfig() {
		AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {
			public void azureusCoreRunning(AzureusCore core) {
			}
		});
	}


	private void 
	initialize(
		Composite ooparent) 
	{
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth 	= 0;
		layout.marginHeight = 0;
		
		ooparent.setLayout( layout);
		
		// if you put the border on the scrolled composite parent then on every resize the computedSize
		// grows by 4 pixels :( So stick in extra composite for the border
		
		final Composite oparent = new Composite( ooparent, SWT.BORDER );
		GridData gridData = new GridData(GridData.FILL_BOTH);
		Utils.setLayoutData(oparent, gridData);
		
		layout = new GridLayout(1, false);
		layout.marginWidth 	= 0;
		layout.marginHeight = 0;
		
		oparent.setLayout( layout);

		final Composite parent = new Composite( oparent, SWT.NULL );
		gridData = new GridData(GridData.FILL_BOTH);
		Utils.setLayoutData(parent, gridData);
		
		layout = new GridLayout(1, false);
		layout.marginWidth 	= 0;
		layout.marginHeight = 0;
		
		parent.setLayout( layout);

		final ScrolledComposite sc = new ScrolledComposite( parent, SWT.V_SCROLL );
		sc.setExpandHorizontal( true );
		sc.setExpandVertical( true );
		sc.addListener( SWT.Resize,
			new Listener() {
				public void handleEvent(Event event) {
					int width = sc.getClientArea().width;
					Point size = parent.computeSize( width, SWT.DEFAULT );
					sc.setMinSize( size );
				}
			} );

		gridData = new GridData(GridData.FILL_BOTH);
		Utils.setLayoutData(sc, gridData);

		composite = new Composite( sc, SWT.NULL );

		sc.setContent( composite );
		
		gridData = new GridData(GridData.FILL_BOTH);
		Utils.setLayoutData(composite, gridData);
		
		layout = new GridLayout(4, false);
		
		composite.setLayout(layout);
		
			// done downloading - 2
		
		ConfigSectionStartShutdown.addDoneDownloadingOption( composite, false );
		
			// max simul down - 2 
		
		Label label = new Label(composite, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalIndent = 8;
		Utils.setLayoutData(label,  gridData );
		Messages.setLanguageText(label, "ConfigView.label.maxdownloads.short");
		
		IntParameter maxDLs = new IntParameter( composite, "max downloads" );
		
			// temporary rates
		
		Group temp_rates = new Group( composite, SWT.NULL );
		Messages.setLanguageText( temp_rates, "label.temporary.rates" );
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;
		
		Utils.setLayoutData(temp_rates, gridData);
		
		layout = new GridLayout(10, false);
		layout.marginWidth 	= 0;
		layout.marginHeight = 0;
		
		temp_rates.setLayout(layout);
		
		//label = new Label(temp_rates, SWT.NULL);
		//Messages.setLanguageText( label, "label.temporary.rates" );
		
		label = new Label(temp_rates, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalIndent=4;
		Utils.setLayoutData(label, gridData);
		Messages.setLanguageText( label, "label.upload.kbps", new String[]{ DisplayFormatters.getRateUnit( DisplayFormatters.UNIT_KB )});

		final IntParameter tempULRate = new IntParameter( temp_rates, "global.download.rate.temp.kbps", 0, Integer.MAX_VALUE );
		
		label = new Label(temp_rates, SWT.NULL);
		Messages.setLanguageText( label, "label.download.kbps", new String[]{ DisplayFormatters.getRateUnit( DisplayFormatters.UNIT_KB )});
		
		final IntParameter tempDLRate = new IntParameter( temp_rates, "global.upload.rate.temp.kbps", 0, Integer.MAX_VALUE );
		
		label = new Label(temp_rates, SWT.NULL);
		Messages.setLanguageText( label, "label.duration.mins" );
		
		final IntParameter tempMins 	= new IntParameter( temp_rates, "global.rate.temp.min", 0, Integer.MAX_VALUE );
				
		final Button activate = new Button( temp_rates, SWT.TOGGLE );
		Messages.setLanguageText( activate, "label.activate" );
		
		final BufferedLabel remLabel = new BufferedLabel(temp_rates, SWT.DOUBLE_BUFFERED );
		gridData = new GridData();
		gridData.widthHint = 150;
		Utils.setLayoutData(remLabel,  gridData );

		activate.addSelectionListener(
			new SelectionAdapter(){
				
				private AzureusCoreLifecycleAdapter	listener;
				
				private TimerEventPeriodic event;
				
				private boolean auto_up_enabled;
				private boolean auto_up_seeding_enabled;
				private boolean seeding_limits_enabled;
				
				private int	up_limit;
				private int	down_limit;
				
				private long	end_time;
				
				public void 
				widgetSelected( SelectionEvent e) 
				{
					if ( activate.getSelection()){
												
						listener = 
							new AzureusCoreLifecycleAdapter()
							{
								public void
								stopping(
									AzureusCore		core )
								{
									deactivate( true );
								}
							};
						
						AzureusCoreFactory.getSingleton().addLifecycleListener( listener );

						Messages.setLanguageText( activate, "FileView.BlockView.Active" );
						
						tempDLRate.setEnabled( false );
						tempULRate.setEnabled( false );
						tempMins.setEnabled( false );
						
					    auto_up_enabled 		= COConfigurationManager.getBooleanParameter( TransferSpeedValidator.AUTO_UPLOAD_ENABLED_CONFIGKEY );
					    auto_up_seeding_enabled = COConfigurationManager.getBooleanParameter( TransferSpeedValidator.AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY );
					    seeding_limits_enabled 	= COConfigurationManager.getBooleanParameter( TransferSpeedValidator.UPLOAD_SEEDING_ENABLED_CONFIGKEY );
					    up_limit 				= COConfigurationManager.getIntParameter( TransferSpeedValidator.UPLOAD_CONFIGKEY );
					    down_limit				= COConfigurationManager.getIntParameter( TransferSpeedValidator.DOWNLOAD_CONFIGKEY );

					    COConfigurationManager.setParameter( TransferSpeedValidator.AUTO_UPLOAD_ENABLED_CONFIGKEY, false );
					    COConfigurationManager.setParameter( TransferSpeedValidator.AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY, false );
					    COConfigurationManager.setParameter( TransferSpeedValidator.UPLOAD_SEEDING_ENABLED_CONFIGKEY, false );
					    
					    COConfigurationManager.setParameter( TransferSpeedValidator.UPLOAD_CONFIGKEY, tempULRate.getValue());
					    COConfigurationManager.setParameter( TransferSpeedValidator.DOWNLOAD_CONFIGKEY, tempDLRate.getValue());

						end_time = SystemTime.getCurrentTime() + tempMins.getValue() * 60* 1000;
						
						event = SimpleTimer.addPeriodicEvent(
							"TempRates",
							1000,
							new TimerEventPerformer() {
								
								public void perform(TimerEvent e) {
									
									Utils.execSWTThread(
										new Runnable(){
											
											public void run() {
											
												if ( event == null ){
													
													return;
												}
												
												long now = SystemTime.getCurrentTime();
												
												long	rem = end_time - now;
												
												if ( rem < 1000 || composite.isDisposed()){
													
													deactivate( false );
													
												}else{
													
													remLabel.setText( MessageText.getString( "TableColumn.header.remaining") + ": " + DisplayFormatters.formatTime( rem ));
												}
											}
										});
								}
							});
					}else{
						
						deactivate( false );
					}
				}
				
				private void
				deactivate(
					boolean	closing )
				{
				    COConfigurationManager.setParameter( TransferSpeedValidator.AUTO_UPLOAD_ENABLED_CONFIGKEY, auto_up_enabled );
				    COConfigurationManager.setParameter( TransferSpeedValidator.AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY, auto_up_seeding_enabled );
				    COConfigurationManager.setParameter( TransferSpeedValidator.UPLOAD_SEEDING_ENABLED_CONFIGKEY, seeding_limits_enabled );

				    COConfigurationManager.setParameter( TransferSpeedValidator.UPLOAD_CONFIGKEY, up_limit  );
				    COConfigurationManager.setParameter( TransferSpeedValidator.DOWNLOAD_CONFIGKEY, down_limit  );
				    
				    if ( !closing ){
				    	
					    if ( listener != null ){
					    	
					    	AzureusCoreFactory.getSingleton().removeLifecycleListener( listener );
					    	
					    	listener = null;
					    }
				    
					    if ( !composite.isDisposed()){
					    	
						    Messages.setLanguageText( activate, "label.activate" );
							activate.setSelection( false );
							
							tempDLRate.setEnabled( true );
							tempULRate.setEnabled( true );
							tempMins.setEnabled( true );
							remLabel.setText( "" );
					    }
				    }
				    
					if ( event != null ){
						event.cancel();
						event = null;
					}
				}
			});
		
		activate.setEnabled( tempMins.getValue() > 0 );
		
		tempMins.addChangeListener(
			new ParameterChangeAdapter()
			{
				@Override
				public void 
				parameterChanged(
					Parameter p,
					boolean caused_internally)
				{
					activate.setEnabled( tempMins.getValue() > 0 );
				}
			});
		
		Utils.execSWTThreadLater(
			100,
			new Runnable() {
				
				public void run() {
					composite.traverse( SWT.TRAVERSE_TAB_NEXT);
				}
			});
	}

	private void delete() {
		Utils.disposeComposite(composite);
	}

	private String getFullTitle() {
		return( MessageText.getString( "label.quick.config" ));
	}
	
	

	private Composite getComposite() {
		return composite;
	}

	private void refresh() {

	}

	public boolean eventOccurred(UISWTViewEvent event) {
    switch (event.getType()) {
      case UISWTViewEvent.TYPE_CREATE:
      	swtView = event.getView();
      	swtView.setTitle(getFullTitle());
        break;

      case UISWTViewEvent.TYPE_DESTROY:
        delete();
        break;

      case UISWTViewEvent.TYPE_INITIALIZE:
        initialize((Composite)event.getData());
        break;

      case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
      	Messages.updateLanguageForControl(getComposite());
      	swtView.setTitle(getFullTitle());
        break;

      case UISWTViewEvent.TYPE_REFRESH:
        refresh();
        break;
      case UISWTViewEvent.TYPE_FOCUSGAINED:{
    	  composite.traverse( SWT.TRAVERSE_TAB_NEXT);
    	  break;
      }
    }

    return true;
  }
}
