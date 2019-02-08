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
 
package org.gudy.azureus2.ui.swt.views.tableitems.mytracker;


import org.gudy.azureus2.core3.tracker.host.*;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.plugins.ui.tables.*;
import org.gudy.azureus2.ui.swt.views.table.CoreTableColumnSWT;

/**
 *
 * @author TuxPaper
 * @since 2.0.8.5
 */
public class DateAddedItem
       extends CoreTableColumnSWT 
       implements TableCellRefreshListener
{
  /** Default Constructor */
  public DateAddedItem() {
    super("date_added", ALIGN_TRAIL, POSITION_LAST, 60, TableManager.TABLE_MYTRACKER);
  }

  public void refresh(TableCell cell) {
	  
    TRHostTorrent item = (TRHostTorrent)cell.getDataSource();
	
    String date_text = "";
    
    if( item != null ) {
	  
    	long	date = item.getDateAdded();
    	
    	date_text = DisplayFormatters.formatDate( date );
    }
    
    cell.setText( date_text );
  }

}