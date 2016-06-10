/*
 * Copyright 2014 Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tudarmstadt.ukp.dkpro.core.stanfordsentiment;

import de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StanfordSentimentAnnotatorTest
{

    //	@Ignore
    @Test
    public void testSentiment()
            throws Exception
    {
        CAS cas = CasCreationUtils
                .createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null);
        cas.setDocumentLanguage("en");
        cas.setDocumentText("I feel very very bad.");

        SimplePipeline.runPipeline(cas,
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(StanfordSentimentAnnotator.class),
                AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class));

        StanfordSentimentAnnotation sentimentAnnotation = JCasUtil
                .select(cas.getJCas(), StanfordSentimentAnnotation.class).iterator().next();
        assertTrue(sentimentAnnotation.getNegative() > 0);
    }

    /*
    @Ignore
	@Test
	public void testSentimentDKProPipe() throws Exception {
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null);
		cas.setDocumentLanguage("en");
		cas.setDocumentText("I feel very very bad.");

		SimplePipeline.runPipeline(cas,
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(StanfordPosTagger.class),
				AnalysisEngineFactory.createEngineDescription(StanfordParser.class)
		);

		Tree tree = TreeUtils.createStanfordTree(JCasUtil.select(cas.getJCas(), ROOT.class).iterator().next());


		SimplePipeline.runPipeline(cas,
				AnalysisEngineFactory.createEngineDescription(StanfordSentimentAnnotator.class),
				AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class));

		SentimentAnnotation sentimentAnnotation = JCasUtil.select(cas.getJCas(), SentimentAnnotation.class).iterator().next();
		assertTrue(sentimentAnnotation.getNegative() > 0);
	}
	*/
}