package io.pedrini.expsplit.domain.group.port.in;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public interface ListPendingInvitationsUseCase {

    List<Group> list(UserProfileId userId);
}
