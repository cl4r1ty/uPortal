/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.rendering;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.cache.CacheKey;
import org.jasig.portal.character.stream.CharacterEventBufferReader;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.CharacterEventSource;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.xml.stream.ChunkingEventReader;

/**
 * Converts a StAX event stream into a {@link CharacterEvent} stream. Breaking up the stream
 * into chunks based on specific elements
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StAXSerializingComponent implements CharacterPipelineComponent {
    private StAXPipelineComponent parentComponent;
    private Map<QName, CharacterEventSource> chunkingElements;
    private Map<Pattern, CharacterEventSource> chunkingPatterns;
    
    public void setParentComponent(StAXPipelineComponent parentComponent) {
        this.parentComponent = parentComponent;
    }
    
    public void setChunkingElements(Map<QName, CharacterEventSource> chunkingElements) {
        this.chunkingElements = chunkingElements;
    }

    public void setChunkingPatterns(Map<String, CharacterEventSource> chunkingPatterns) {
        final Map<Pattern, CharacterEventSource> compiledChunkingPatterns = new LinkedHashMap<Pattern, CharacterEventSource>();
        
        for (final Map.Entry<String, CharacterEventSource> chunkingPatternEntry : chunkingPatterns.entrySet()) {
            final String key = chunkingPatternEntry.getKey();
            final Pattern pattern = Pattern.compile(key);
            final CharacterEventSource value = chunkingPatternEntry.getValue();
            compiledChunkingPatterns.put(pattern, value);
        }
        
        this.chunkingPatterns = compiledChunkingPatterns;
    }

    @Override
    public CacheableEventReader<CharacterEventReader, CharacterEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final CacheableEventReader<XMLEventReader, XMLEvent> eventReader = this.parentComponent.getEventReader(request, response);

        //Writer shared by the ChunkingEventReader and the StAX Serializer
        final StringWriter writer = new StringWriter();
        
        //Add the chunking wrapper to the XMLEventReader
        final XMLEventReader xmlEventReader = eventReader.getEventReader();
        final ChunkingEventReader chunkingEventReader = new ChunkingEventReader(this.chunkingElements, this.chunkingPatterns, xmlEventReader, writer); 
        
        //Setup the serializer
        //TODO pooling of output factories?
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLEventWriter xmlEventWriter;
        try {
            xmlEventWriter = outputFactory.createXMLEventWriter(writer);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLEventWriter", e);
        }
        
        try {
            xmlEventWriter.add(chunkingEventReader);
            xmlEventWriter.flush();
            xmlEventWriter.close();
            chunkingEventReader.close();
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write events to Writer", e);
        }
        
        //Return the chunked data
        final CacheKey cacheKey = this.getCacheKey(request, response);
        final List<CharacterEvent> characterEvents = chunkingEventReader.getCharacterEvents();
        final CharacterEventBufferReader characterEventReader = new CharacterEventBufferReader(characterEvents.listIterator());
        return new CacheableEventReaderImpl<CharacterEventReader, CharacterEvent>(cacheKey, characterEventReader);
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.parentComponent.getCacheKey(request, response);
    }
}