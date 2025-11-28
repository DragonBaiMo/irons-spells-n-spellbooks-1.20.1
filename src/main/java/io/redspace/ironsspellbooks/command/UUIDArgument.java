package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class UUIDArgument implements ArgumentType<UUID> {
    private static final List<String> EXAMPLES = Arrays.asList(
        "550e8400-e29b-41d4-a716-446655440000",
        "6ec9c4c9-fc5c-4f42-b2c9-6b745d4764e2"
    );

    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    public static UUIDArgument uuid() {
        return new UUIDArgument();
    }

    public static UUID getUUID(CommandContext<?> context, String name) {
        return context.getArgument(name, UUID.class);
    }

    @Override
    public UUID parse(final StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        while (reader.canRead() && (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '-')) {
            reader.skip();
        }

        String uuidString = reader.getString().substring(start, reader.getCursor());

        if (!UUID_PATTERN.matcher(uuidString).matches()) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, uuidString);
        }

        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, uuidString);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
