import { TestBed } from '@angular/core/testing';

import { StompServiceService } from './stomp.service';

describe('StompServiceService', () => {
  let service: StompServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StompServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
