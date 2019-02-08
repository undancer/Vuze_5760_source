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
 */

package com.aelitis.azureus.ui.swt.columns.subscriptions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.gudy.azureus2.ui.swt.views.table.TableCellSWT;
import org.gudy.azureus2.ui.swt.views.table.TableCellSWTPaintListener;

import com.aelitis.azureus.ui.common.table.TableColumnCore;
import com.aelitis.azureus.ui.swt.imageloader.ImageLoader;
import com.aelitis.azureus.ui.swt.subscriptions.SBC_SubscriptionResult;

import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.plugins.ui.tables.*;

/**
 * @author TuxPaper
 * @created Sep 25, 2008
 *
 */
public class ColumnSubResultNew
	implements TableCellSWTPaintListener, TableCellAddedListener,
	TableCellRefreshListener, TableCellMouseListener
{
	public static final String COLUMN_ID = "new";

	private static int WIDTH = 38; // enough to fit title

	private static Image imgNew;

	private static Image imgOld;


	public ColumnSubResultNew(TableColumn column ) {
	
		column.initialize(TableColumn.ALIGN_CENTER, TableColumn.POSITION_LAST, WIDTH );
		column.addListeners(this);
		column.setRefreshInterval(TableColumn.INTERVAL_GRAPHIC);
		column.setType(TableColumn.TYPE_GRAPHIC);

		if ( column instanceof TableColumnCore ){
			
			((TableColumnCore)column).addCellOtherListener("SWTPaint", this );
		}
		
		imgNew = ImageLoader.getInstance().getImage("image.activity.unread");
		imgOld = ImageLoader.getInstance().getImage("image.activity.read");
	}

	public void cellPaint(GC gc, TableCellSWT cell) {
		SBC_SubscriptionResult entry = (SBC_SubscriptionResult) cell.getDataSource();

		Rectangle cellBounds = cell.getBounds();
		Image img = entry== null || entry.getRead() ? imgOld: imgNew;

		if (img != null && !img.isDisposed()) {
			Rectangle imgBounds = img.getBounds();
			gc.drawImage(img, cellBounds.x
					+ ((cellBounds.width - imgBounds.width) / 2), cellBounds.y
					+ ((cellBounds.height - imgBounds.height) / 2));
		}
	}

	public void cellAdded(TableCell cell) {
		cell.setMarginWidth(0);
		cell.setMarginHeight(0);
		
		if ( cell instanceof TableCellSWT ){
		
			((TableCellSWT)cell).setCursorID( SWT.CURSOR_HAND );
		}
	}

	public void refresh(TableCell cell) {
		SBC_SubscriptionResult entry = (SBC_SubscriptionResult)cell.getDataSource();

		if ( entry != null ){
			
			boolean unread = !entry.getRead();
			
			long sortVal = ((unread ? 2 : 1) << 62) + (SystemTime.getCurrentTime()-entry.getTime())/1000;
	
			if (!cell.setSortValue(sortVal) && cell.isValid()) {
				return;
			}
		}
	}

	public void cellMouseTrigger(final TableCellMouseEvent event) {
		if (event.eventType == TableRowMouseEvent.EVENT_MOUSEDOWN
				&& event.button == 1) {
			SBC_SubscriptionResult entry = (SBC_SubscriptionResult) event.cell.getDataSource();
			
			if ( entry != null ){
			
				entry.setRead(!entry.getRead());
			
				event.cell.invalidate();
			}
		}
	}
}
