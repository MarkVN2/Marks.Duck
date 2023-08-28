package net.byteboost.duck.functions;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.ParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import net.byteboost.duck.ApiKeys;
import net.byteboost.duck.App;

// import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;

public class  Loader {
    
    //Transforms DOCX,PDF,TXT,PPT to Document
    public static Document toDoc(String file){
    Path filePath = toPath(file);
    Document document = FileSystemDocumentLoader.loadDocument(filePath);
    return document;
    }

    //Loads the file and asks a question to the AI that returns the awnser
    public static String loadIntoOpenAI(Document file, String question){

        Document document = file;

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .splitter(new ParagraphSplitter())
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY))
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                // .chatMemory() // you can override default chat memory
                // .promptTemplate() // you can override default prompt template
                .build();

        String answer = chain.execute(question);
        return answer; 
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = App.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}


