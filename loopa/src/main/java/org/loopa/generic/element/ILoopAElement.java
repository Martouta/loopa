/*******************************************************************************
 *  Copyright (c) 2017 Universitat Politécnica de Catalunya (UPC)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *  	Edith Zavala
 *******************************************************************************/

package org.loopa.generic.element;

import java.util.Map;

import org.loopa.element.receiver.IReceiver;

public interface ILoopAElement {
	public IReceiver getReceiver();
	
	public void setElementRecipients(Map<String, Object> r);

	public Map<String, Object> getElementRecipients();

	public void addRecipient(String id, Object o);

	public void removeRecipient(String id);
	
	public String getElementId();
}
