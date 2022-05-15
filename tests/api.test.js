const app = require("../app");
const supertest = require("supertest");
const { uniqueNamesGenerator, adjectives, colors, animals } = require('unique-names-generator');

const randomName1 = uniqueNamesGenerator({ dictionaries: [adjectives, colors, animals] });
const randomName2 = uniqueNamesGenerator({ dictionaries: [adjectives, colors, animals] });

const data = {
  "userID": randomName1,
  "availability": {
    "duration": 30,
    "fromTime": "2022-05-05T18:51:34.708Z",
    "toTime": "2022-07-05T18:52:45.186Z",
    "skipDays": [1, 2, 3]
  }
}

test("POST /set/username/", async () => {
  await supertest(app).post(`/set/username/${randomName1}`)
    .expect(200)
    .then((response) => {
      expect(response.body).toEqual({ success: randomName1 })
    });
});

test("POST /set/availability", async() => {
  await supertest(app).post('/set/availability')
    .send(data)
    .expect(200)
    .then(async (response) => {
      expect(response.body).toEqual({ success: true })
    })
})

test("GET /get/availability", async() => {
  await supertest(app).get(`/get/availability/${randomName1}`)
    .expect(200)
    .then(async (response) => {
      delete response.body['createdAt']
      delete response.body['updatedAt']
      expect(response.body).toEqual({
        "userID": randomName1,
        "duration": 30,
        "from": "2022-05-05T18:51:34.708Z",
        "to": "2022-07-05T18:52:45.186Z",
        "skipDays": [1, 2, 3]
      })
    })
})

// test("POST /api/posts", async () => {
//   const data = { title: "Post 1", content: "Lorem ipsum" };

//   await supertest(app).post("/api/posts")
//     .send(data)
//     .expect(200)
//     .then(async (response) => {
//       // Check the response
//       expect(response.body._id).toBeTruthy();
//       expect(response.body.title).toBe(data.title);
//       expect(response.body.content).toBe(data.content);

//       // Check data in the database
//       const post = await Post.findOne({ _id: response.body._id });
//       expect(post).toBeTruthy();
//       expect(post.title).toBe(data.title);
//       expect(post.content).toBe(data.content);
//     });
// });

// test("GET /api/posts/:id", async () => {
//   const post = await Post.create({ title: "Post 1", content: "Lorem ipsum" });

//   await supertest(app).get("/api/posts/" + post.id)
//     .expect(200)
//     .then((response) => {
//       expect(response.body._id).toBe(post.id);
//       expect(response.body.title).toBe(post.title);
//       expect(response.body.content).toBe(post.content);
//     });
// });

// test("PATCH /api/posts/:id", async () => {
//   const post = await Post.create({ title: "Post 1", content: "Lorem ipsum" });

//   const data = { title: "New title", content: "dolor sit amet" };

//   await supertest(app).patch("/api/posts/" + post.id)
//     .send(data)
//     .expect(200)
//     .then(async (response) => {
//       // Check the response
//       expect(response.body._id).toBe(post.id);
//       expect(response.body.title).toBe(data.title);
//       expect(response.body.content).toBe(data.content);

//       // Check the data in the database
//       const newPost = await Post.findOne({ _id: response.body._id });
//       expect(newPost).toBeTruthy();
//       expect(newPost.title).toBe(data.title);
//       expect(newPost.content).toBe(data.content);
//     });
// });
