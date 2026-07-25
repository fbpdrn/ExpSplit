package io.pedrini.expsplit.domain.group.port.in;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface InviteMemberUseCase {

    Group invite(GroupId groupId, UserProfileId inviterId, UserProfileId inviteeId);
}
