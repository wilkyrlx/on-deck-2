import { Event } from '../model/Event'
import { Sport } from '../model/Sport'
import { Team } from '../model/Team';

// @ts-ignore
// delay used from https://stackoverflow.com/questions/38956121/how-to-add-delay-to-promise-inside-then
export class MockRepository implements EventsRepository {
    getEvents(teamPreferences: Team[]): Promise<Event[]> {
        return new Promise(resolve => setTimeout(resolve, 400))
        .then(() => mockEvents)
    }
    getHighlightGames(teamPreferences: Team[]): Promise<Event[]> {
        return new Promise(resolve => setTimeout(resolve, 400))
        .then(() => mockEvents)
    }
}

export const mockEvents: Event[] = [
    new Event(
        "test1",
        { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
        { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
        addHours(new Date(), 1),
        addHours(new Date(), 3),
        Sport.NFL
    ),
    new Event(
        "test2",
        { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
        { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
        addHours(new Date(), 5),
        addHours(new Date(), 7),
        Sport.NFL
    ),
    new Event(
        "test3",
        { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
        { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
        addHours(new Date(), 9),
        addHours(new Date(), 11),
        Sport.NFL
    )
]

export const mockSavedTeams: Team[] = [
    { name: "Pittsburgh Steelers", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL },
    { name: "Baltimore Ravens", iconUrl: "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/pit.png", sport: Sport.NFL }
]

export function addHours(date: Date, hours: number) {
    var copiedDate = new Date(date.getTime());
    copiedDate.setHours(copiedDate.getHours() + hours);
    return copiedDate;
}