package aedifi.bene.api.service;

import aedifi.bene.api.command.CommandSpec;
import aedifi.bene.api.module.ModuleId;

public interface Commands {
    void register(ModuleId owner, CommandSpec specification);

    void unregisterOwnerCommands(ModuleId owner);
}
