package ru.kontur.vostok.hercules.protocol.decoder;

import ru.kontur.vostok.hercules.protocol.Container;
import ru.kontur.vostok.hercules.protocol.Variant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ContainerReader implements Reader<Container> {

    public static final ContainerReader INSTANCE = readAllTags();

    private static final VariantReader VARIANT_READER = VariantReader.INSTANCE;

    private final Set<String> tags;

    private ContainerReader(Set<String> tags) {
        this.tags = tags;
    }

    public static ContainerReader readAllTags() {
        return new ContainerReader(null);
    }

    public static ContainerReader readTags(Set<String> tags) {
        return new ContainerReader(tags);
    }

    @Override
    public Container read(Decoder decoder) {
        short length = decoder.readShort();
        Map<String, Variant> variantMap = new HashMap<>(length);
        while (0 <= --length) {
            String tagName = decoder.readString();
            if (Objects.isNull(tags) || tags.contains(tagName)) {
                Variant variant = VARIANT_READER.read(decoder);
                variantMap.put(tagName, variant);
            } else {
                VARIANT_READER.skip(decoder);
            }
        }
        return new Container(variantMap);
    }

    @Override
    public int skip(Decoder decoder) {
        int skipped = 0;
        short length = decoder.readShort();
        skipped += SizeOf.SHORT;
        while (0 <= --length) {
            skipped += decoder.skipString();
            skipped += VARIANT_READER.skip(decoder);
        }
        return skipped;
    }
}
