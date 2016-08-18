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


import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;


public class PublishAgent implements TransactionClientListener
{

	/** UserProfile */
	protected UserAgentProfile user_profile;
	private Logger logger = Logger.getLogger(getClass().getCanonicalName());
	/** SipProvider */
	protected SipProvider sip_provider;
	public String status;
	//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences();
	//Boolean publish_enable_status  = prefs.getBoolean("publish_enable",true);
	Boolean publish_enable_status=true;

	/** Costructs a new MessageAgent. */
	public PublishAgent(SipProvider sip_provider, UserAgentProfile user_profile)
	{  this.sip_provider=sip_provider;
		this.user_profile=user_profile;

	}
	public void publish()
	{
		status="open";
	}
	public void publish(String status, int expireTime, String note) {

		if (publish_enable_status == true) {
			this.status=status;
			MessageDigest md = null;
			String tupleId;
			try {
				md = MessageDigest.getInstance("MD5");
				byte[] md5bytes = md.digest(user_profile.username.getBytes());
				tupleId = md5bytes.toString();
			} catch (NoSuchAlgorithmException e) {
				tupleId=user_profile.username;
				e.printStackTrace();
			}

			String from = user_profile.username;
			String entity = "sip:" + user_profile.username;
			String xml =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
							"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
							"entity=\"" + entity + "\">" +
							"<tuple id=\"" + tupleId + "\">" +
							"<status>" +
							"<basic>" + status + "</basic>" +
							"<note>" + note + "</note>" +
							"</status>" +
							"</tuple>" +
							"</presence>";
			MessageFactory msgf = new MessageFactory();
			Message req = msgf.createPublishRequest(sip_provider, new NameAddress(from), "presence", expireTime, "application/pidf+xml", xml);
			TransactionClient t = new TransactionClient(sip_provider, req, this);
			t.request();
		}
	}

	public void unPublish(int expireTime, String from) {
		if (publish_enable_status == true) {
			MessageFactory msgf = new MessageFactory();
			Message req = msgf.createPublishRequest(sip_provider, new NameAddress(from), "presence", expireTime, null, null);
			TransactionClient t = new TransactionClient(sip_provider, req, this);
			t.request();
		}
	}

	public void onTransSuccessResponse(TransactionClient tc, Message resp)
	{
	}
	public void onTransFailureResponse(TransactionClient tc, Message resp) {

	}

	@Override
	public void onTransTimeout(TransactionClient tc) {

	}

	public void onTransProvisionalResponse(TransactionClient tc, Message resp)
	{
	}


}
