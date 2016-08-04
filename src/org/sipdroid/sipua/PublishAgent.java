/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.sipdroid.sipua;


import org.sipdroid.sipua.ui.MessageSendingRequest;
import org.zoolu.sip.address.*;
import org.zoolu.sip.authentication.DigestAuthentication;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.transaction.*;
import org.zoolu.sip.header.AuthorizationHeader;
import org.zoolu.sip.header.ProxyAuthenticateHeader;
import org.zoolu.sip.header.ProxyAuthorizationHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.WwwAuthenticateHeader;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;

import java.io.*;
import java.util.logging.Logger;



public class PublishAgent implements TransactionClientListener
{

   /** UserProfile */
   protected UserAgentProfile user_profile;
	private Logger logger = Logger.getLogger(getClass().getCanonicalName());
   /** SipProvider */
   protected SipProvider sip_provider;

   /** Message listener */
   protected MessageAgentListener listener;


   /** Costructs a new MessageAgent. */
   public PublishAgent(SipProvider sip_provider, UserAgentProfile user_profile, MessageAgentListener listener)
   {  this.sip_provider=sip_provider;
      this.listener=listener;
      this.user_profile=user_profile;

   }

	public void publish(String status, String note, int expireTime)
	{

		String tupleId=sip_provider.UAIdentity.replace("/", "").replace(":", "").toLowerCase();
		String from = username+"@"+realm;
		String entity="sip:"+username+"@"+realm;
		String xml=
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
						"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\""+
						"entity=\""+ entity +"\">"+
						"<tuple id=\""+ tupleId +"\">"+
						"<status>"+
						"<basic>"+status+"</basic>"+
						"</status>"+
				"</tuple>"+
						"</presence>";
		MessageFactory msgf = new MessageFactory();
		Message req = msgf.createPublishRequest(sip_provider, new NameAddress(from), "presence", expireTime,"application/pidf+xml",xml);
		TransactionClient t = new TransactionClient(sip_provider, req, this);
		t.request();
		run();

	}

	public void onTransSuccessResponse(TransactionClient tc, Message resp)
	{  onDeliverySuccess(tc,resp.getStatusLine().getReason());
	}
	public void onTransFailureResponse(TransactionClient tc, Message resp) {

	}
	public void onTransProvisionalResponse(TransactionClient tc, Message resp)
	{  // do nothing.
	}

	/** When the TransactionClient goes into the "Terminated" state, caused by transaction timeout */
	public void onTransTimeout(TransactionClient tc)
	{  onDeliveryFailure(tc,"Timeout");
	}
	private void onDeliverySuccess(TransactionClient tc, String result) {
		logger.info("Message successfully delivered ("+result+").");
		MessageTransactionClient mtc = (MessageTransactionClient)tc;
		Message req=tc.getRequestMessage();
		NameAddress recipient=req.getToHeader().getNameAddress();
		String subject=null;
		if (req.hasSubjectHeader()) subject=req.getSubjectHeader().getSubject();
		if (listener!=null) listener.onMaDeliverySuccess(this,recipient,subject,result);
		mtc.getMessageSendingRequest().onSuccess();
	}
}
