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
package org.loopa.element.receiver;

import org.loopa.element.receiver.messageprocessor.IMessageProcessor;
import org.loopa.generic.documents.managers.IPolicyManager;
import org.loopa.comm.message.IMessage;

public class Receiver extends AReceiver{

	public Receiver(String id, IPolicyManager policyManager, IMessageProcessor imm) {
		super(id, policyManager, imm);
		// TODO Auto-generated constructor stub
	}

	public void doOperation(IMessage message) {
		System.out.println( "TODO" );
	}

}
