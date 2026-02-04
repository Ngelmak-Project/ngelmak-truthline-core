package org.ngelmakproject.repository;

import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkMembership;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkMembership entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MembershipRepository extends JpaRepository<NkMembership, Long> {
  @Query("SELECT m FROM NkMembership m WHERE m.following.id = :followingId AND m.follower.id = :followerId")
  Optional<NkMembership> findOneByFollowingAndFollower(@Param("followingId") Long following, @Param("followerId") Long follower);

  Optional<NkMembership> findOneByFollowingAndFollower(NkAccount following, NkAccount follower);

  List<NkMembership> findByFollowing(NkAccount following);
  List<NkMembership> findByFollower(NkAccount follower);
}
