package uk.gov.justice.services.core.jms.converter;

import uk.gov.justice.services.core.jms.exception.JmsConverterException;
import uk.gov.justice.services.core.util.JsonObjectConverter;
import uk.gov.justice.services.messaging.Envelope;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Implementation of {@link MessageConverter} for {@link uk.gov.justice.services.messaging.Envelope}
 */
@ApplicationScoped
public class EnvelopeConverter implements MessageConverter<Envelope, TextMessage> {

    static final String JMS_HEADER_CPPNAME = "CPPNAME";

    @Inject
    JsonObjectConverter jsonObjectConverter;

    @Override
    public Envelope fromMessage(final TextMessage message) {
        String messageAsString = null;

        try {
            messageAsString = message.getText();
            return jsonObjectConverter.asEnvelope(jsonObjectConverter.fromString(messageAsString));
        } catch (JMSException e) {
            throw createJmsConverterException(message, e);
        }
    }

    @Override
    public TextMessage toMessage(final Envelope envelope, final Session session) {
        final String envelopeAsString = jsonObjectConverter.asString(jsonObjectConverter.fromEnvelope(envelope));

        try {
            final TextMessage textMessage = session.createTextMessage(envelopeAsString);
            textMessage.setStringProperty(JMS_HEADER_CPPNAME, envelope.metadata().name());
            return textMessage;
        } catch (JMSException e) {
            throw new JmsConverterException(String.format("Exception while creating message from envelope %s", envelopeAsString), e);
        }
    }

    private JmsConverterException createJmsConverterException(final TextMessage message, final Throwable e) {
        try {
            return new JmsConverterException(String.format("Exception while creating envelope from message %s", message.getJMSMessageID()), e);
        } catch (JMSException e1) {
            return new JmsConverterException(String.format("Exception while creating envelope from message. Failed to retrieve messageId from %s", message), e1);
        }
    }

}