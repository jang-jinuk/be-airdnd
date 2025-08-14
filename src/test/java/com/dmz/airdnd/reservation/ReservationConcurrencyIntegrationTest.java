package com.dmz.airdnd.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dmz.airdnd.accommodation.domain.Accommodation;
import com.dmz.airdnd.accommodation.repository.AccommodationRepository;
import com.dmz.airdnd.common.auth.UserContext;
import com.dmz.airdnd.common.auth.dto.UserInfo;
import com.dmz.airdnd.fixture.TestReservationFactory;
import com.dmz.airdnd.reservation.dto.request.ReservationRequest;
import com.dmz.airdnd.reservation.repository.ReservationRepository;
import com.dmz.airdnd.reservation.service.ReservationService;
import com.dmz.airdnd.user.domain.Role;

@SpringBootTest
public class ReservationConcurrencyIntegrationTest {

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private AccommodationRepository accommodationRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	@DisplayName("1번 유저가 먼저 예약을 요청하면 항상 1번 유저가 예약을 성공한다.")
	void first_requester_should_always_win() throws InterruptedException {
		// given
		ReservationRequest reservationRequest = TestReservationFactory.createReservationRequest();
		Accommodation accommodation = accommodationRepository.findById(1L).get();

		int threadCount = 10;
		CountDownLatch user1ReadyLatch = new CountDownLatch(1);
		CountDownLatch othersReadyLatch = new CountDownLatch(threadCount - 1);
		CountDownLatch endLatch = new CountDownLatch(threadCount);
		AtomicInteger successUserId = new AtomicInteger(0);

		// when
		// 1번 유저 가장 먼저 시작
		new Thread(() -> {
			try {
				UserContext.set(new UserInfo(1L, Role.USER));

				user1ReadyLatch.countDown(); // 1번 유저 준비 완료
				Thread.sleep(1); // 아주 짧은 시간 먼저 시작

				reservationService.booking(accommodation.getId(), reservationRequest);
				successUserId.compareAndSet(0, 1); // 첫 번째 성공자만 기록

			} catch (Exception e) {
				System.err.println("User 1: " + e.getMessage());
			} finally {
				UserContext.clear();
				endLatch.countDown();
			}
		}).start();

		// 나머지 9명의 유저들은 1번 유저보다 약간 늦게 시작
		for (int i = 2; i <= threadCount; i++) {
			final long userId = i;

			new Thread(() -> {
				try {
					user1ReadyLatch.await(); // 1번 유저가 준비될 때까지 대기

					UserContext.set(new UserInfo(userId, Role.USER));

					reservationService.booking(accommodation.getId(), reservationRequest);
					successUserId.compareAndSet(0, (int)userId);

				} catch (Exception e) {
					System.err.println("User " + userId + ": " + e.getMessage());
				} finally {
					UserContext.clear();
					othersReadyLatch.countDown();
					endLatch.countDown();
				}
			}).start();
		}

		endLatch.await(10, TimeUnit.SECONDS);

		// then
		assertThat(successUserId.get()).isEqualTo(1);
	}
}
