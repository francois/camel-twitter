package org.apache.camel.component.twitter;

import java.util.regex.Pattern;

import org.apache.camel.component.twitter.consumer.Twitter4JConsumer;
import org.apache.camel.component.twitter.consumer.directmessage.DirectMessageConsumer;
import org.apache.camel.component.twitter.consumer.search.SearchConsumer;
import org.apache.camel.component.twitter.consumer.streaming.FilterConsumer;
import org.apache.camel.component.twitter.consumer.streaming.SampleConsumer;
import org.apache.camel.component.twitter.consumer.timeline.HomeConsumer;
import org.apache.camel.component.twitter.consumer.timeline.MentionsConsumer;
import org.apache.camel.component.twitter.consumer.timeline.PublicConsumer;
import org.apache.camel.component.twitter.consumer.timeline.RetweetsConsumer;
import org.apache.camel.component.twitter.consumer.timeline.UserConsumer;
import org.apache.camel.component.twitter.data.ConsumerType;
import org.apache.camel.component.twitter.data.StreamingType;
import org.apache.camel.component.twitter.data.TimelineType;
import org.apache.camel.component.twitter.data.TrendsType;
import org.apache.camel.component.twitter.producer.DirectMessageProducer;
import org.apache.camel.component.twitter.producer.MockProducer;
import org.apache.camel.component.twitter.producer.UserProducer;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * URI STRUCTURE:
 * 
 * timeline/
 * 		public
 * 		home
 * 		friends
 * 		user (PRODUCER)
 * 		mentions
 * 		retweetsofme
 * user/
 * 		search users (DIRECT ONLY)
 * 		user suggestions (DIRECT ONLY)
 * trends/
 * 		daily
 * 		weekly
 * userlist
 * directmessage (PRODUCER)
 * streaming/
 * 		filter (POLLING ONLY)
 * 		sample (POLLING ONLY)
 * 
 * @author Brett E. Meyer (3RiverDev.com)
 */
public class Twitter4JFactory {
	private static final transient Log LOG = LogFactory.getLog(Twitter4JFactory.class);

	public static Twitter4JConsumer getConsumer(TwitterEndpoint te, String uri) throws IllegalArgumentException {
		String[] uriSplit = splitUri(uri);
		
		if (uriSplit.length > 0) {
			switch (ConsumerType.fromUri(uriSplit[0])) {
			case DIRECTMESSAGE:
				return new DirectMessageConsumer(te);
			case SEARCH:
				if (te.getProperties().getKeywords() == null || te.getProperties().getKeywords().trim().isEmpty()) {
					throw new IllegalArgumentException(
							"Type set to SEARCH but no keywords were provided.");
				} else {
					return new SearchConsumer(te);
				}
			case STREAMING:
				switch (StreamingType.fromUri(uriSplit[1])) {
				case SAMPLE:
					return new SampleConsumer(te);
				case FILTER:
					return new FilterConsumer(te);
				}
				break;
			case TIMELINE:
				if (uriSplit.length > 1) {
					switch (TimelineType.fromUri(uriSplit[1])) {
					case HOME:
						return new HomeConsumer(te);
					case MENTIONS:
						return new MentionsConsumer(te);
					case PUBLIC:
						return new PublicConsumer(te);
					case RETWEETSOFME:
						return new RetweetsConsumer(te);
					case USER:
						if (te.getProperties().getUser() == null || te.getProperties().getUser().trim().isEmpty()) {
							throw new IllegalArgumentException(
									"Fetch type set to USER TIMELINE but no user was set.");
						} else {
							return new UserConsumer(te);
						}
					}
				}
				break;
			case TRENDS:
				if (uriSplit.length > 1) {
					switch (TrendsType.fromUri(uriSplit[1])) {
					case DAILY:
						// TODO
						break;
					case WEEKLY:
						// TODO
						break;
					}
				}
				break;
			case USER:
				// TODO
				break;
			case USERLIST:
				// TODO
				break;
			}
		}
		
		LOG.warn("A consumer type was not provided (or an incorrect pairing was used).  Defaulting to Public Timeline!");
		return new PublicConsumer(te);
	}
	
	public static DefaultProducer getProducer(TwitterEndpoint te, String uri) throws IllegalArgumentException {
		String[] uriSplit = splitUri(uri);
		
		if (uriSplit.length > 0) {
			switch (ConsumerType.fromUri(uriSplit[0])) {
			case DIRECTMESSAGE:
				if (te.getProperties().getUser() == null || te.getProperties().getUser().trim().isEmpty()) {
					throw new IllegalArgumentException(
							"Producer type set to DIRECT MESSAGE but no recipientuser was set.");
				} else {
					return new DirectMessageProducer(te);
				}
			case TIMELINE:
				if (uriSplit.length > 1) {
					switch (TimelineType.fromUri(uriSplit[1])) {
					case USER:
						return new UserProducer(te);
					}
				}
			}
		}
		
		LOG.warn("A producer type was not provided (or an incorrect pairing was used).  Defaulting to a MOCK!");
		return new MockProducer(te);
	}
	
	private static String[] splitUri(String uri) {
		Pattern p1 = Pattern.compile("twitter:(//)*");
		Pattern p2 = Pattern.compile("\\?.*");

		uri = p1.matcher(uri).replaceAll("");
		uri = p2.matcher(uri).replaceAll("");
		
		return uri.split("/");
	}
}