package org.ngelmakproject.repository;

import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.Account;
import org.ngelmakproject.domain.Membership;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Membership entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
  @Query("SELECT m FROM Membership m WHERE m.following.id = :followingId AND m.follower.id = :followerId")
  Optional<Membership> findOneByFollowingAndFollower(@Param("followingId") Long following, @Param("followerId") Long follower);

  Optional<Membership> findOneByFollowingAndFollower(Account following, Account follower);

  List<Membership> findByFollowing(Account following);
  List<Membership> findByFollower(Account follower);
}
