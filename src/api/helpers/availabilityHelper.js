function sortDatesCompare(a,b) {
    if (new Date(a.start) > new Date(b.start)) {
        return 1;
    } else {
        return -1;
    }
}

function calculateUserAvailabilityOverlap (userAAvailability, userBAvailability) {
    if (userAAvailability.length == 0 || userBAvailability.length == 0)
        return [];
    overlapAvailabilityWindow = [];
    i = 0;
    j = 0;
    AStart = userAAvailability[0].start;
    AEnd = userAAvailability[0].end;
    BStart = userBAvailability[0].start;
    BEnd = userBAvailability[0].end;
    while (i < userAAvailability.length && j < userBAvailability.length) {
        if(new Date(BStart) > new Date(AEnd)) {
                i = i + 1
                if ( i < userAAvailability.length ) {
                    AStart = userAAvailability[i].start
                    AEnd = userAAvailability[i].end
                }
                continue;
        }

        if (new Date(BStart) < new Date(AStart))
            BStart = AStart

        if (new Date(BEnd) <= new Date(AEnd)) {
            overlapAvailabilityWindow.push([ BStart, BEnd ])
            j = j + 1
            AStart = BEnd
            if(j < userBAvailability.length) {
                BStart = userBAvailability[j].start
                BEnd = userBAvailability[j].end
            }
        } else {
            overlapAvailabilityWindow.push([ BStart, AEnd ])
            Bstart = AEnd + 1
            i = i + 1
            if(i < userAAvailability.length) {
                AStart = userAAvailability[i].start
                AEnd = userAAvailability[i].end
            }
        }
    }
    return overlapAvailabilityWindow;
}

module.exports = {
    sortDatesCompare, calculateUserAvailabilityOverlap
}
