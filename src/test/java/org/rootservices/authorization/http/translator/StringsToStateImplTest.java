package org.rootservices.authorization.http.translator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.rootservices.authorization.http.validator.HasOneItem;
import org.rootservices.authorization.http.validator.IsNotNull;
import org.rootservices.authorization.persistence.entity.ResponseType;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StringsToStateImplTest {

    @Mock
    private IsNotNull mockIsNotNull;

    @Mock
    private HasOneItem mockHasOneItem;

    private StringsToStateImpl subject;

    @Before
    public void run() {
        subject = new StringsToStateImpl(mockIsNotNull, mockHasOneItem);
    }

    @Test
    public void runIsOk() throws ValidationError {
        String expected = "some-state";
        List<String> items = new ArrayList<>();
        String item = expected;
        items.add(item);

        when(mockIsNotNull.run(items)).thenReturn(true);
        when(mockHasOneItem.run(items)).thenReturn(true);

        String actual = subject.run(items);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void runEmptyList() throws ValidationError {
        List<String> items = new ArrayList<>();

        when(mockIsNotNull.run(items)).thenReturn(true);
        when(mockHasOneItem.run(items)).thenReturn(true);

        String actual = subject.run(items);
        assertThat(actual).isNull();
    }

    @Test(expected=ValidationError.class)
    public void runIsNull() throws ValidationError {
        List<String> items = null;

        when(mockIsNotNull.run(items)).thenReturn(false);

        subject.run(items);
    }

    @Test(expected=ValidationError.class)
    public void runHasTooManyItems() throws ValidationError {
        List<String> items = new ArrayList<>();
        String item = "items";
        items.add(item);
        items.add(item);

        when(mockIsNotNull.run(items)).thenReturn(true);
        when(mockHasOneItem.run(items)).thenReturn(false);

        subject.run(items);
    }

    @Test(expected=ValidationError.class)
    public void runIsEmpty() throws ValidationError {
        List<String> items = new ArrayList<>();
        String item = "";
        items.add(item);

        when(mockIsNotNull.run(items)).thenReturn(true);
        when(mockHasOneItem.run(items)).thenReturn(true);

        subject.run(items);
    }
}